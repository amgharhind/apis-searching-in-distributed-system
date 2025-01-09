package com.example.demo.controllers;

import com.example.demo.entities.Document;
import com.example.demo.services.ClusterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    /**
     * Get cluster health
     */
    @GetMapping("/health")
    public ResponseEntity<?> getClusterHealth() {
        try {
            return ResponseEntity.ok(clusterService.getClusterHealth());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching cluster health: " + e.getMessage());
        }
    }

    /**
     * Get information about all Elasticsearch nodes
     */
    @GetMapping("/nodes")
    public ResponseEntity<?> getAllNodesInfo() {
        try {
            return ResponseEntity.ok(clusterService.getAllNodesInfo());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching nodes info: " + e.getMessage());
        }
    }

    /**
     * Get stats for all nodes
     */
    @GetMapping("/nodes/stats")
    public ResponseEntity<?> getNodesStats() {
        try {
            return ResponseEntity.ok(clusterService.getNodesStats());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching nodes stats: " + e.getMessage());
        }
    }

    /**
     * Get stats for a specific node
     */
    @GetMapping("/nodes/{nodeId}/stats")
    public ResponseEntity<?> getNodeStats(@PathVariable("nodeId") String nodeId) {
        try {
            return ResponseEntity.ok(clusterService.getNodeContent(nodeId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching node stats: " + e.getMessage());
        }
    }


    /**
     * Check if a specific node is running
     */
    @GetMapping("/nodes/{nodeId}/status")
    public ResponseEntity<?> isNodeRunning(@PathVariable("nodeId") String nodeId) {
        try {
            boolean isRunning = clusterService.isNodeRunning(nodeId);
            return ResponseEntity.ok("Node " + nodeId + " is " + (isRunning ? "running" : "not running"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking node status: " + e.getMessage());
        }
    }



    /**
     * Get documents from a specific node.
     */
    @GetMapping("/nodes/{nodeId}/documents")
    public ResponseEntity<?> getDocumentsInNode(@PathVariable("nodeId") String nodeId) {
        try {
            var documents = clusterService.getDocumentsInNode(nodeId);
            if (documents.isEmpty()) {
                return ResponseEntity.status(404).body("No documents found on node " + nodeId);
            }
            return ResponseEntity.ok(documents);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching documents for node " + nodeId + ": " + e.getMessage());
        }
    }
}
