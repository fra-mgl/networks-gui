package main

import (
	"github.com/gin-gonic/gin"
	"go-backend/database"
)

const PORT = "4000"
const RYU_LINKS_ENDPOINT = "http://localhost:8080/topology/links"
const RYU_NOTIFICATION_ENDPOINT = "http://localhost:8000/notification"

func main() {
	// Get a database connection
	dbConn := database.InitDB()

	// Set up the REST API
	router := gin.Default()
	router.GET("/notification", ryuNotification(dbConn))
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
