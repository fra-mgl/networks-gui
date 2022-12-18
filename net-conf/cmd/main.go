package main

import (
	"encoding/json"
	"fmt"
	"github.com/gin-gonic/gin"
	"io"
	"net/http"
	"netconf"
	"netconf/database"
	"strconv"
	"time"
)

const PORT = "4000"
const RYU_LINKS_ENDPOINT = "http://ryu:8080/topology/links"
const RYU_NOTIFICATION_ENDPOINT = "http://ryu:8080/notification"

func main() {
	// Get a database connection
	dbConn := database.InitDB()

	// Set up the REST API
	router := gin.Default()
	router.GET("/allDataPathIps", allDataPathIps(dbConn))
	router.GET("/allIpTables", getAllIpTables(dbConn))
	router.GET("/dataPathIps/:dpid", dataPathIps(dbConn))
	router.GET("/ipTable/:dpid", getIpTable(dbConn))
	router.POST("/netConf", configureNetwork(dbConn))

	// Start the server
	if err := router.Run(":" + PORT); err != nil {
		panic(err)
	}
}

/* The following closures return a request handler that has access to a database connection */

// Returns the IP addresses of all the routers

func allDataPathIps(dbConn *database.DbConn) func(*gin.Context) {
	return func(c *gin.Context) {
		datapathMap, err := dbConn.GetIPsGroupedByDataPath()
		if err != nil {
			c.AbortWithError(500, err)
		} else {
			c.IndentedJSON(http.StatusOK, datapathMap)
		}
	}
}

// Returns the IP addresses of a particular router

func dataPathIps(dbConn *database.DbConn) func(*gin.Context) {
	return func(c *gin.Context) {
		dpidStr := c.Param("dpid")
		dpid, err := strconv.Atoi(dpidStr)
		if err != nil {
			c.AbortWithError(400, fmt.Errorf("invalid datapath id %s", dpidStr))
		}

		ipAddresses, err := dbConn.GetDataPathIPs(int64(dpid))
		if err != nil {
			c.AbortWithError(500, err)
		} else {
			c.IndentedJSON(http.StatusOK, ipAddresses)
		}
	}
}

// Returns the ip routing table of the given router

func getIpTable(dbConn *database.DbConn) func(*gin.Context) {
	return func(c *gin.Context) {
		dpidStr := c.Param("dpid")
		dpid, err := strconv.Atoi(dpidStr)
		if err != nil {
			c.AbortWithError(400, fmt.Errorf("invalid datapath id %s", dpidStr))
		}

		routingTable, err := dbConn.GetIpTable(int64(dpid))
		if err != nil {
			c.AbortWithError(500, err)
		} else {
			c.IndentedJSON(http.StatusOK, routingTable)
		}
	}
}

// Returns the ip routing tables for all routers in the network

func getAllIpTables(dbConn *database.DbConn) func(ctx *gin.Context) {
	return func(c *gin.Context) {
		ipTables, err := dbConn.AllIpTables()
		if err != nil {
			c.AbortWithError(500, err)
		} else {
			c.IndentedJSON(http.StatusOK, ipTables)
		}
	}
}

// Saves a new network configuration

func configureNetwork(dbConn *database.DbConn) func(*gin.Context) {
	type portData struct {
		DataPathID string  `json:"dpid" binding:"required"`
		PortNo     string  `json:"port_no" binding:"required"`
		IpAddress  string `json:"ip" binding:"required"`
	}

	return func(c *gin.Context) {
		portsData := make([]portData, 0)
		if err := c.BindJSON(&portsData); err != nil {
			c.AbortWithError(400, err)
			return
		}
		switchesPorts := make([]database.SwitchPort, len(portsData))
		for i, port := range portsData {
			dpid, err := netconf.HexStrToInt(port.DataPathID)
			if err != nil {
				c.AbortWithError(400, err)
				return
			}
			portNo, err := netconf.HexStrToInt(port.PortNo)
			if err != nil {
				c.AbortWithError(400, err)
				return
			}
			switchesPorts[i].DataPathID = dpid
			switchesPorts[i].PortNo = int32(portNo)
			switchesPorts[i].IpAddress = port.IpAddress
		}

		// The input network configuration is saved
		if err := dbConn.SaveNetworkConfiguration(switchesPorts); err != nil {
			c.AbortWithError(500, err)
		} else {
			// A goroutine is spawned to build the ip tables for the switches
			// and notify Ryu of the available L3 configuration
			if err := l3config(dbConn); err != nil {
				c.AbortWithError(500, err)
			}

			c.AbortWithStatus(http.StatusOK)
		}
	}
}

// The function should be called as a goroutine, because it panics
// in case of errors. It performs queries the Ryu controller to know the links
// among routers in the network, then it computes the routing tables and notifies
// the controller

func l3config(dbConn *database.DbConn) error {
	// Simple struct that represent the expected json format
	type portData struct {
		DpId   string `json:"dpid" binding:"required"`
		PortNo string `json:"port_no" binding:"required"`
		HwAddr string `json:"hw_addr" binding:"required"`
		Name   string `json:"name" binding:"required"`
	}

	// Query the Ryu controller for link information between routers
	httpClient := http.Client{Timeout: time.Duration(1) * time.Second}
	res, err := httpClient.Get(RYU_LINKS_ENDPOINT)
	if err != nil {
		return err
	}
	response, err := io.ReadAll(res.Body)
	if err != nil {
		return err
	}
	jsonBody := make([]map[string]portData, 0)
	if err = json.Unmarshal(response, &jsonBody); err != nil {
		return err
	}
	
	// The database records are built
	links := make([]database.Link, 0)
	for _, item := range jsonBody {
		srcDpID, err := netconf.HexStrToInt(item["src"].DpId)
		if err != nil {
			return err
		}
		srcPort, err := netconf.HexStrToInt(item["src"].PortNo)
		if err != nil {
			return err
		}
		dstDpID, err := netconf.HexStrToInt(item["dst"].DpId)
		if err != nil {
			return err
		}
		dstPort, err := netconf.HexStrToInt(item["dst"].PortNo)
		if err != nil {
			return err
		}

		newLink := database.Link{
			SrcDataPathID: srcDpID,
			SrcPortNo:     int32(srcPort),
			DstDataPathID: dstDpID,
			DstPortNo:     int32(dstPort),
		}
		links = append(links, newLink)
	}
	if err = dbConn.SaveLinks(links); err != nil {
		return err
	}

	// Now build the ip routing tables, and then notify the Ryu controller
	err = dbConn.BuildIpTables()
	if err != nil {
		return err
	}
	_, err = httpClient.Get(RYU_NOTIFICATION_ENDPOINT)
	if err != nil {
		return err
	}
	return nil
}
