package database

// Database table that records ip tables entries for the OF switches

type IpTableRecord struct {
	DataPathID          int64     `gorm:"not null;index:,type:hash"`
	DataPath            DataPath  `gorm:"ForeignKey:DataPathID;"`
	DestinationSubNet   string    `gorm:"not null;"`
	DestinationSubNetFK IpAddress `gorm:"ForeignKey:DestinationSubNet;"`
	NextHop             string    `gorm:"not null;"`
	NextHopFK           IpAddress `gorm:"ForeignKey:NextHop;"`
}

// The function build the ip tables for all OF switches in the network

func (dbConn *DbConn) BuildIpTables() error {
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return err
	}
	_, err = rawConn.Exec("drop table ip_table_records")
	return err
}

// The function builds the network graph

func (dbConn *DbConn) buildGraph() (map[int64][]string, []string, error) {
	portData := make([]PortData, 0)
	err := dbConn.gormConn.Find(&portData).Error
	if err != nil {
		return nil, nil, err
	}

	graph := make(map[int64][]string)
	groundSubnets := make([]string, 0)
	for _, el := range portData {
		if _, ok := graph[el.DataPathID]; !ok {
			graph[el.DataPathID] = make([]string, 0)
		}
		// Record all ground 'ground' subnets, and insert in the graph a link from their
		// gateway router
		if el.NextHop == nil {
			groundSubnets = append(groundSubnets, el.PortAddress)
			graph[el.DataPathID] = append(graph[el.DataPathID], el.PortAddress)
		}
	}

	return graph, groundSubnets, nil
}
