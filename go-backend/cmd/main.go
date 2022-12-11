package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"go-backend/database"
	"net/http"
	"strconv"
)

const PORT = "4000"

func main() {
	// Get a database connection
	dbConn := database.InitDB()

	// Set up the REST API
	router := gin.Default()
	router.GET("/allDataPathIps", allDataPathIps(dbConn))
	router.GET("/dataPathIps/:dpid", dataPathIps(dbConn))
	router.GET("/buildIpTables", buildIpTables(dbConn))

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
