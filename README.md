# Distributed Search System Architecture 

This project is a comprehensive, scalable search system built with modern technologies to deliver fast and semantically relevant results. It integrates a distributed Elasticsearch cluster, Redis caching, and a sophisticated two-stage re-ranking pipeline featuring BM25 and DistilBERT. The entire system is orchestrated by a central Spring Boot application and is containerized with Docker for seamless deployment and scalability.

## Key Features

-   **Distributed Search:** Utilizes an Elasticsearch cluster for robust, scalable indexing and document retrieval.
-   **Advanced Search Capabilities:** Supports a wide range of queries including wildcard, exact phrase, proximity, and range searches.
-   **Two-Stage Re-ranking:**
    1.  **BM25:** Fast initial ranking performed by Elasticsearch.
    2.  **DistilBERT:** Deep semantic re-ranking performed by a dedicated service to refine results based on contextual relevance.
-   **High-Performance Caching:** Integrates Redis to cache query results, significantly reducing latency and optimizing user experience.
-   **Dockerized Deployment:** All services are containerized, ensuring easy and consistent deployment across any environment.

## System Architecture

The system is built on a microservices architecture:

-   **Spring Boot Application:** The core of the system, handling all incoming search requests, orchestrating calls to other services, and managing caching logic.
-   **Elasticsearch Cluster:** A three-node cluster (`es01`, `es02`, `es03`) serves as the primary engine for indexing documents and performing initial BM25-ranked searches.
-   **Flask Service (Python):** A lightweight microservice dedicated to the computationally intensive task of semantic re-ranking using a DistilBERT model.
-   **Redis:** An in-memory data store used for caching query results and user interaction scores.

## Services Overview

-   **SearchService (Spring Boot):** Creates and executes dynamic Elasticsearch queries, supporting everything from simple keyword matching to complex pattern and proximity searches.
-   **BM25RankingService (Spring Boot):** Interacts with Elasticsearch to retrieve the initial list of documents ranked by their BM25 scores.
-   **ReRankingService (Spring Boot):** Communicates with the external Flask service to apply semantic re-ranking to the initial results.
-   **InteractionsReRankingService (Spring Boot):** Enhances ranking by combining search results with user interaction data stored in Redis.
-   **RedisCacheService (Spring Boot):** Manages all caching operations, including storing results, validating cache consistency, and clearing entries.
-   **Flask Service (Python):** Exposes a `/re-rank` endpoint that accepts a list of documents and a query, then returns a re-ranked list based on DistilBERT's semantic understanding.

## Technology 

-   **Backend:** Spring Boot
-   **Search & Indexing:** Elasticsearch
-   **Caching:** Redis
-   **Ranking & NLP:** DistilBERT, SentenceTransformers, BM25
-   **ML Service:** Flask
-   **Deployment:** Docker

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/apis-searching-in-distributed-system
    ```
