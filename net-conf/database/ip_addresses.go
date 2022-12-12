package database

import (
	netconf "go-backend"
	"gorm.io/gorm"
)

// Links between OF switches

type Link struct {
	SrcDataPathID int64 `gorm:"not null"`
	SrcPortNo     int32 `gorm:"not null"`
	DstDataPathID int64 `gorm:"not null"`
	DstPortNo     int32 `gorm:"not null"`
}

// Assignment of ip addresses to switches ports

type SwitchPort struct {
	DataPathID int64  `gorm:"not null;uniqueIndex:switchPort" json:"dpid" binding:"required"`
	PortNo     int32  `gorm:"not null;uniqueIndex:switchPort" json:"port_no" binding:"required"`
	IpAddress  string `gorm:"not null;uniqueIndex:uniqueIP" json:"ip" binding:"required"`
}

// This function is called by Gorm before saving a record of the 'switch_port'
// table. It checks that the input ip is of the form '10.0.0.1/24'

func (p *SwitchPort) BeforeSave(tx *gorm.DB) error {
	nip := netconf.NetMaskedIp{Str: p.IpAddress}
	return nip.Validate()
}

// This function is a wrapper to a database query to create a batch of
// 'switch_ports' table records

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

// A wrapper to a database query to create a batch of 'links' table records

func (dbConn *DbConn) SaveLinks(link []Link) error {
	return dbConn.gormConn.Create(&link).Error
}

// The function returns all IP addresses assigned to the ports of an OpenFlow switch .
// They are provided in the format { 'port number' : 'ip address' }

func (dbConn *DbConn) GetDataPathIPs(dp int64) (map[int32]string, error) {
	ports := make([]SwitchPort, 0)
	if err := dbConn.gormConn.Find(&ports, "data_path_id = ?", dp).Error; err != nil {
		return nil, err
	}

	// build the desired output
	portMapping := make(map[int32]string)
	for _, port := range ports {
		portMapping[port.PortNo] = port.IpAddress
	}
	return portMapping, nil
}

// The function returns a map that relates OpenFlow switches, identified by their
// data path ID, to the ip addresses of their ports.

func (dbConn *DbConn) GetIPsGroupedByDataPath() (map[int64]map[int32]string, error) {
	ports := make([]SwitchPort, 0)
	if err := dbConn.gormConn.Find(&ports).Error; err != nil {
		return nil, err
	}

	// Build the desired output
	output := make(map[int64]map[int32]string)
	for _, port := range ports {
		if _, ok := output[port.DataPathID]; !ok {
			output[port.DataPathID] = make(map[int32]string)
		}
		output[port.DataPathID][port.PortNo] = port.IpAddress
	}
	return output, nil
}
