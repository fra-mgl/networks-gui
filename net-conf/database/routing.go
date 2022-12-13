package database

import "netconf"

// Database relation that records iptables entries for the OF switches.
// It also represents links between ports of different routers. When
// the `NextHop` fields are nil, the record represents a link between a router
// and a ground network.

type IpTableRecord struct {
	DestinationSubNet string `gorm:"not null;"`
	DataPathID        int64  `gorm:"not null;index:,type:hash"`
	PortNo            int32  `gorm:"not null;"`
	PortAddress       string `gorm:"not null;"`
	NextHopDataPathID *int64
	NextHopPortNo     *int32
	NextHopAddress    *string
}

func (r *IpTableRecord) isGround() bool {
	return r.NextHopDataPathID == nil
}

// Computes the ip routing tables for all the routers in the network.
//    - it first builds a graph representing links between routers
//    - then for each router, it traverses the graph to compute its
//		routing table

func (dbConn *DbConn) BuildIpTables() error {
	// All the ip tables will be re-computed, so the current values are deleted
	// from the database
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return err
	}
	_, err = rawConn.Exec("delete from ip_table_records")
	if err != nil {
		return err
	}

	// Compute the graph of connections between routers
	graph, err := dbConn.buildNetworkGraph()
	if err != nil {
		return err
	}

	// For each router, do...
	for startRouter, links := range graph {
		// Depth-First-Search
		parents := make(map[int64]int64)
		for router := range graph {
			parents[router] = -1
		}
		parents[startRouter] = startRouter
		queue := make([]int64, 0)

		// If the 'root' router is directly connected to some ground network, the
		// ip table entry for that network is immediately written to the database.
		// All routers reachable from the root are inserted in the queue.
		for _, link := range links {
			if link.isGround() {
				if err := dbConn.gormConn.Create(&link).Error; err != nil {
					return err
				}
			} else {
				parents[*link.NextHopDataPathID] = *link.NextHopDataPathID
				queue = append(queue, *link.NextHopDataPathID)
			}
		}

		// The graph is traversed. When a ground network is reached, a lookup of
		// the parents table determines which link was followed from the 'root' router
		// to reach the ground network.
		for len(queue) != 0 {
			currRouter := queue[0]
			queue = queue[1:] // dequeue

			for _, link := range graph[currRouter] {
				if link.isGround() && currRouter != startRouter {
					subNet := link.DestinationSubNet
					// Lookup in the parents table which link was taken to get to this router
					nextHop := parents[currRouter]
					for _, link := range links {
						if !link.isGround() && *link.NextHopDataPathID == nextHop {
							// Save the ip table entry
							link.DestinationSubNet = subNet
							if err := dbConn.gormConn.Create(&link).Error; err != nil {
								return err
							}
							break
						}
					}

				} else if !link.isGround() && parents[*link.NextHopDataPathID] == -1 {
					// Links that have yet to be visited are enqueued. The information
					// about which link was taken from the 'root' router to get to the
					// current router is propagated

					parents[*link.NextHopDataPathID] = parents[currRouter]
					queue = append(queue, *link.NextHopDataPathID)
				}
			}
		}
	}
	return nil
}

// Struct representing the output json format of an ip routing table record

type jsonIpTableEntry struct {
	SrcPortNo     int32  `json:"src_port_no"`
	SrcIp         string `json:"src_ip"`
	DstPortNo     int32  `json:"dst_port_no"`
	DstIp         string `json:"dst_ip"`
	DstDataPathId int64  `json:"dst_dpid"`
}

// Gets the ip routing tables of all routers in the network. The output is represented as:
// {
//	   'dpid':
// 		   {
//	 		   'destination network' : {
//	 				'src_port_no',
//	 				 'src_ip',
//	 				 'dst_port_no',
//	 				 'dst_ip'
// 					 'dst_dpid'
//	 			}
//	 		}
// }

func (dbConn *DbConn) AllIpTables() (map[int64]map[string]jsonIpTableEntry, error) {
	ipTableRecords := make([]IpTableRecord, 0)
	if err := dbConn.gormConn.Find(&ipTableRecords).Error; err != nil {
		return nil, err
	}

	output := make(map[int64]map[string]jsonIpTableEntry, 0)
	for _, record := range ipTableRecords {
		if _, ok := output[record.DataPathID]; !ok {
			output[record.DataPathID] = make(map[string]jsonIpTableEntry)
		}
		// The json object is built
		var dstPortNo int32
		var dstIp string
		var dstDpId int64
		if record.isGround() {
			dstPortNo = 0
			dstIp = ""
			dstDpId = 0
		} else {
			dstPortNo = *record.NextHopPortNo
			dstIp = *record.NextHopAddress
			dstDpId = *record.NextHopDataPathID
		}
		output[record.DataPathID][record.DestinationSubNet] = jsonIpTableEntry{
			SrcPortNo:     record.PortNo,
			SrcIp:         record.PortAddress,
			DstPortNo:     dstPortNo,
			DstIp:         dstIp,
			DstDataPathId: dstDpId,
		}
	}
	return output, nil
}

// Queries the ip routing table of the given switch. The output is represented as:
// {
//	   'destination network' : {
//	 	  'src_port_no',
//	      'src_ip',
//	 	  'dst_port_no',
//	 	  'dst_ip'
//	   }
// }

func (dbConn *DbConn) GetIpTable(dpid int64) (map[string]jsonIpTableEntry, error) {
	ipTableRecords := make([]IpTableRecord, 0)
	if err := dbConn.gormConn.Find(&ipTableRecords, "data_path_id = ?", dpid).Error; err != nil {
		return nil, err
	}

	output := make(map[string]jsonIpTableEntry, 0)
	for _, record := range ipTableRecords {
		var dstPortNo int32
		var dstIp string
		var dstDpId int64
		if record.isGround() {
			dstPortNo = 0
			dstIp = ""
			dstDpId = 0
		} else {
			dstPortNo = *record.NextHopPortNo
			dstIp = *record.NextHopAddress
		}
		output[record.DestinationSubNet] = jsonIpTableEntry{
			SrcPortNo:     record.PortNo,
			SrcIp:         record.PortAddress,
			DstPortNo:     dstPortNo,
			DstIp:         dstIp,
			DstDataPathId: dstDpId,
		}
	}
	return output, nil
}

// The function builds the network graph

func (dbConn *DbConn) buildNetworkGraph() (map[int64][]IpTableRecord, error) {
	links := make([]IpTableRecord, 0)
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return nil, err
	}

	// First query the links between routers
	query := `select port1.data_path_id, port1.port_no, port1.ip_address, port2.data_path_id, port2.port_no, port2.ip_address
			  from links, switch_ports as port1, switch_ports as port2
			  where links.src_data_path_id = port1.data_path_id and links.src_port_no = port1.port_no and
			  links.dst_data_path_id = port2.data_path_id and links.dst_port_no = port2.port_no`
	res, err := rawConn.Query(query)
	if err != nil {
		return nil, err
	}
	for res.Next() {
		row := IpTableRecord{
			NextHopDataPathID: new(int64),
			NextHopPortNo:     new(int32),
			NextHopAddress:    new(string),
		}
		err = res.Scan(&row.DataPathID, &row.PortNo, &row.PortAddress,
			row.NextHopDataPathID, row.NextHopPortNo, row.NextHopAddress)
		if err != nil {
			return nil, err
		}
		links = append(links, row)
	}

	// Then find out, among all routers, which ports have been assigned an IP address, but
	// are not connected to another router. Those ports lead to ground networks
	query = `select port.data_path_id, port.port_no, port.ip_address from switch_ports as port, links where 
             port.data_path_id = links.src_data_path_id and port.port_no = links.src_port_no and 
			 (links.dst_data_path_id, links.dst_port_no) not in (select port1.data_path_id, 
			 port1.port_no from switch_ports as port1)`
	res, err = rawConn.Query(query)
	if err != nil {
		return nil, err
	}
	for res.Next() {
		row := IpTableRecord{}
		err = res.Scan(&row.DataPathID, &row.PortNo, &row.PortAddress)
		if err != nil {
			return nil, err
		}
		netAddress := netconf.NetMaskedIp{Str: row.PortAddress}
		netAddress, err = netAddress.GetNetAddress()
		if err != nil {
			return nil, err
		}
		row.DestinationSubNet = netAddress.Str
		links = append(links, row)
	}

	// The graph is built by mapping routers to their links to other routers, and to
	// their direct links to ground subnetworks
	graph := make(map[int64][]IpTableRecord)
	for _, link := range links {
		if _, ok := graph[link.DataPathID]; !ok {
			graph[link.DataPathID] = make([]IpTableRecord, 0)
		}
		graph[link.DataPathID] = append(graph[link.DataPathID], link)
	}
	return graph, nil
}
