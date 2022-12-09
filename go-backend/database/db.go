package database

import "gorm.io/gorm"

// Wrapper type to a database connection

type DbConn struct {
	gormConn *gorm.DB
}

// The function returns all IP addresses owned by a OpenFlow switch

func (dbConn *DbConn) GetDataPathIPs(dp int64) ([]IpAddress, error) {
	ipAddresses := make([]IpAddress, 2)
	err := dbConn.gormConn.Find(&ipAddresses, "data_path_id = ?", dp).Error
	if err != nil {
		return nil, err
	}
	return ipAddresses, nil
}

// The function returns a map that relates OpenFlow switches, identified by their
// data path ID.

func (dbConn *DbConn) GetIPsGroupedByDataPath() (map[int64][]IpAddress, error) {
	ipAddresses := make([]IpAddress, 8)
	addressesMap := make(map[int64][]IpAddress)
	err := dbConn.gormConn.Find(&ipAddresses).Error
	if err != nil {
		return nil, err
	}

	// The map that relates data path ids to ip addresses is built
	for i := range ipAddresses {
		address := ipAddresses[i]
		addressesMap[address.DataPathID] = append(addressesMap[address.DataPathID], address)
	}
	return addressesMap, nil
}
