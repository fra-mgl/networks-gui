package database

import (
	"gorm.io/gorm"
)

// Links between OF switches

type Link struct {
	SrcDataPathID int `gorm:"not null"`
	SrcPortNo     int `gorm:"not null"`
	DstDataPathID int `gorm:"not null"`
	DstPortNo     int `gorm:"not null"`
}

// Assignment of ip addresses to switches ports

type SwitchPort struct {
	DataPathID int    `gorm:"not null;uniqueIndex:switchPort" json:"dpid" binding:"required"`
	PortNo     int    `gorm:"not null;uniqueIndex:switchPort" json:"port_no" binding:"required"`
	IpAddress  string `gorm:"not null;uniqueIndex:uniqueIP" json:"ip" binding:"required"`
}

// This function is called by Gorm before saving a record of the 'switch_port'
// table. It checks that the input ip is of the form '10.0.0.1/24'

func (p *SwitchPort) BeforeSave(tx *gorm.DB) error {
	nip := netMaskedIp{p.IpAddress}
	return nip.validate()
}

// This function is a wrapper to a database query to create a 'switch_ports'
// table record

func (dbConn *DbConn) SaveNetworkConfiguration(ports []SwitchPort) error {
	// The previous network configuration is deleted
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return err
	}
	_, err = rawConn.Exec("delete from switch_ports")
	if err != nil {
		return err
	}
	return dbConn.gormConn.Create(&ports).Error
}

type portIpJoin struct {
	dataPathID int64
	ip         string
	portNo     int
}

// The function returns all IP addresses owned by a OpenFlow switch, and related next hop
// addresses. They are provided in the format { 'port number' : 'ip address' }

func (dbConn *DbConn) GetDataPathIPs(dp int64) (map[int]string, error) {
	// join tables 'port_data', 'ip_addresses' to get the all the ip addresses
	// of the selected switch
	joinResult := make([]portIpJoin, 0)
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return nil, err
	}
	query := `select data_path_id, address, port_no from port_data, ip_addresses where port_data.port_address = ip_addresses.address and port_data.data_path_id = $1;`
	res, err := rawConn.Query(query, dp)
	if err != nil {
		return nil, err
	}
	for res.Next() {
		row := portIpJoin{}
		err = res.Scan(&row.dataPathID, &row.ip, &row.portNo)
		if err != nil {
			return nil, err
		}
		joinResult = append(joinResult, row)
	}

	// build the desired output
	portMapping := make(map[int]string)
	for _, el := range joinResult {
		portMapping[el.portNo] = el.ip
	}
	return portMapping, nil
}

// The function returns a map that relates OpenFlow switches, identified by their
// data path ID, to the ip addresses of their ports.

func (dbConn *DbConn) GetIPsGroupedByDataPath() (map[int64]map[int]string, error) {
	// join tables 'port_data', 'ip_addresses' to get the ip addresses of
	// all ports for all OF switches
	joinResult := make([]portIpJoin, 0)
	rawConn, err := dbConn.gormConn.DB()
	if err != nil {
		return nil, err
	}
	res, err := rawConn.Query(`select data_path_id, address, port_no from port_data, ip_addresses where port_data.port_address = ip_addresses.address;`)
	if err != nil {
		return nil, err
	}
	for res.Next() {
		row := portIpJoin{}
		err = res.Scan(&row.dataPathID, &row.ip, &row.portNo)
		if err != nil {
			return nil, err
		}
		joinResult = append(joinResult, row)
	}

	// Build the desired output by performing a sort of 'nested loop join'
	output := make(map[int64]map[int]string)
	for _, el := range joinResult {
		_, ok := output[el.dataPathID]
		if !ok {
			output[el.dataPathID] = make(map[int]string)
		}
		output[el.dataPathID][el.portNo] = el.ip
	}
	return output, nil
}
