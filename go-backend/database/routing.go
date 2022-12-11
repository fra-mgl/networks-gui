package database

// Database relation that records iptables entries for the OF switches.
// It also represents links between ports of different routers. When
// the `NextHop` fields are nil, the record represents a link between a router
// and a ground network.

type IpTableRecord struct {
	DataPathID        int64  `gorm:"not null;index:,type:hash"`
	DestinationSubNet string `gorm:"not null;"`
	PortMAC           string `gorm:"not null;"`
	PortAddress       string `gorm:"not null;"`
	NextHopDataPathID *int64
	NextHopMAC        *string
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

// The function builds the network graph

func (dbConn *DbConn) buildNetworkGraph() (map[int64][]IpTableRecord, error) {
	links := make([]IpTableRecord, 0)
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return nil, err
	}

	// First query the links between routers
	query := `select port1.data_path_id, ip1.address, ip1.mac, port2.data_path_id, ip2.address, ip2.mac
			  from port_data as port1, port_data as port2, ip_addresses as ip1, ip_addresses as ip2
			  where port1.next_hop = port2.port_address and port1.port_address = ip1.address and
			  port2.port_address = ip2.address`
	res, err := rawConn.Query(query)
	if err != nil {
		return nil, err
	}
	for res.Next() {
		row := IpTableRecord{
			NextHopDataPathID: new(int64),
			NextHopAddress:    new(string),
			NextHopMAC:        new(string),
		}
		err = res.Scan(&row.DataPathID, &row.PortAddress, &row.PortMAC,
			row.NextHopDataPathID, row.NextHopAddress, row.NextHopMAC)
		if err != nil {
			return nil, err
		}
		links = append(links, row)
	}

	// Then query the links between routers and their adjacent ground subnets
	query = `select port.data_path_id, ip.address, ip.mac from port_data as port, 
             ip_addresses as ip where port.next_hop is null and port.port_address = ip.address`
	res, err = rawConn.Query(query)
	if err != nil {
		return nil, err
	}
	for res.Next() {
		row := IpTableRecord{}
		err = res.Scan(&row.DataPathID, &row.PortAddress, &row.PortMAC)
		if err != nil {
			return nil, err
		}
		netAddress := netMaskedIp{row.PortAddress}
		netAddress, err = netAddress.getNetAddress()
		if err != nil {
			return nil, err
		}
		row.DestinationSubNet = netAddress.str
		links = append(links, row)
	}

	// The graph is built by mapping routers to their links to other routers
	// or ground subnetworks
	graph := make(map[int64][]IpTableRecord)
	for _, link := range links {
		if _, ok := graph[link.DataPathID]; !ok {
			graph[link.DataPathID] = make([]IpTableRecord, 0)
		}
		graph[link.DataPathID] = append(graph[link.DataPathID], link)
	}
	return graph, nil
}
