package main

import (
	"encoding/json"
	"fmt"
	"github.com/gin-gonic/gin"
	"go-backend"
	"go-backend/database"
	"io"
	"net/http"
	"strconv"
	"time"
)

/* The following closures return a request handler that has access to a database connection */

// Returns the IP addresses of all the routers

func allDataPathIps(dbConn *database.DbConn) func(*gin.Context) {
	return func(c *gin.Context) {
		datapathMap, err := dbConn.GetIPsGroupedByDataPath()
		if err != nil {
			c.AbortWithError(500, fmt.Errorf("internal error"))
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
			c.AbortWithError(500, fmt.Errorf("internal error"))
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
			c.AbortWithError(500, fmt.Errorf("internal error"))
		} else {
			c.IndentedJSON(http.StatusOK, routingTable)
		}
	}
}

// Builds the ip tables for all routers in the network

func buildIpTables(dbConn *database.DbConn) func(*gin.Context) {
	return func(c *gin.Context) {
		err := dbConn.BuildIpTables()
		if err != nil {
			c.AbortWithError(500, fmt.Errorf("internal error"))
		} else {
			c.AbortWithStatus(http.StatusOK)
		}
	}
}

// Saves a new network configuration

func configureNetwork(dbConn *database.DbConn) func(*gin.Context) {
	return func(c *gin.Context) {
		portsData := make([]database.SwitchPort, 0)
		if err := c.BindJSON(&portsData); err != nil {
			c.AbortWithError(500, err)
			return
		}
		if err := dbConn.SaveNetworkConfiguration(portsData); err != nil {
			c.AbortWithError(500, err)
		} else {
			c.AbortWithStatus(http.StatusOK)
		}
	}
}

// When the ryu controller sends a notification to the Go backend, it answers
// by requesting to the controller the links between the switches in the network

func ryuNotification(dbConn *database.DbConn) func(*gin.Context) {
	type portData struct {
		DpId   string `json:"dpid" binding:"required"`
		PortNo string `json:"port_no" binding:"required"`
		HwAddr string `json:"hw_addr" binding:"required"`
		Name   string `json:"name" binding:"required"`
	}
	return func(c *gin.Context) {
		// A request to ryu is made
		httpClient := http.Client{Timeout: time.Duration(1) * time.Second}
		res, err := httpClient.Get(RYU_ENDPOINT)
		if err != nil {
			c.AbortWithError(500, nil)
			panic(err)
		}
		response, err := io.ReadAll(res.Body)
		if err != nil {
			c.AbortWithError(500, nil)
			panic(err)
		}
		jsonBody := make([]map[string]portData, 0)
		if err = json.Unmarshal(response, &jsonBody); err != nil {
			c.AbortWithError(500, nil)
			panic(err)
		}

		// The database records are built
		links := make([]database.Link, 0)
		for _, item := range jsonBody {
			srcDpID, err := netconf.HexStrToInt(item["src"].DpId)
			if err != nil {
				c.AbortWithError(500, nil)
				panic(err)
			}
			srcPort, err := netconf.HexStrToInt(item["src"].PortNo)
			if err != nil {
				c.AbortWithError(500, nil)
				panic(err)
			}
			dstDpID, err := netconf.HexStrToInt(item["dst"].DpId)
			if err != nil {
				c.AbortWithError(500, nil)
				panic(err)
			}
			dstPort, err := netconf.HexStrToInt(item["dst"].PortNo)
			if err != nil {
				c.AbortWithError(500, nil)
				panic(err)
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
			c.AbortWithError(500, nil)
			panic(err)
		}

		// Finally, send a response to Ryu
		c.AbortWithStatus(http.StatusOK)
	}
}
