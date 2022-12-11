package main

import (
	"github.com/gin-gonic/gin"
	"go-backend/database"
)

const PORT = "4000"
const RYU_ENDPOINT = "http://localhost:8080/topology/links"

func main() {
	// Get a database connection
	dbConn := database.InitDB()

	// Set up the REST API
	router := gin.Default()
	router.GET("/ryuNotification", ryuNotification(dbConn))
	router.GET("/allDataPathIps", allDataPathIps(dbConn))
	router.GET("/dataPathIps/:dpid", dataPathIps(dbConn))
	router.GET("/getIpTable/:dpid", getIpTable(dbConn))
	router.GET("/buildIpTables", buildIpTables(dbConn))
	router.POST("/netConf", configureNetwork(dbConn))

	// Start the server
	if err := router.Run(":" + PORT); err != nil {
		panic(err)
	}
}
