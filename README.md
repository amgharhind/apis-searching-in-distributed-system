# Distributed Search System with Advanced Re-ranking

## Overview

This project implements a comprehensive and scalable distributed search system designed to deliver fast and semantically relevant results. It leverages a microservices architecture, integrating a distributed Elasticsearch cluster for efficient indexing and retrieval, Redis for high-performance caching, and a sophisticated two-stage re-ranking pipeline. The system is orchestrated by a central Spring Boot application and containerized using Docker for seamless deployment and scalability.

##  Motivation and Importance

In today's data-rich environment, traditional keyword-based search often falls short in providing truly relevant results. Users expect intelligent search capabilities that understand context and intent. This project addresses this need by combining the speed and scalability of Elasticsearch with the semantic understanding of modern NLP models. The distributed nature ensures high availability and fault tolerance, crucial for enterprise-level applications, while the re-ranking mechanism significantly enhances the quality and relevance of search outcomes.

## Key Features

*   **Distributed Search:** Utilizes a three-node Elasticsearch cluster for robust, scalable indexing and document retrieval, ensuring high availability and performance.
*   **Advanced Search Capabilities:** Supports a wide range of queries including wildcard, exact phrase, proximity, and range searches, providing flexibility for complex search requirements.
*   **Two-Stage Re-ranking:**
    1.  **BM25:** Fast initial ranking performed by Elasticsearch, quickly narrowing down the most relevant documents.
    2.  **DistilBERT:** Deep semantic re-ranking performed by a dedicated Python Flask service, refining results based on contextual relevance and improving the quality of the final output.
*   **High-Performance Caching:** Integrates Redis to cache frequently accessed query results, significantly reducing latency, minimizing load on backend services, and optimizing user experience.
*   **Dockerized Deployment:** All services are containerized using Docker, ensuring easy, consistent, and portable deployment across various environments.

## System Architecture

The system is built on a microservices architecture, with each component serving a specific role:


![architecture](https://github.com/user-attachments/assets/bde92ec2-cad4-4ded-9214-e00dc6039d87)


*   **Spring Boot Application (Core Service):** The central orchestrator of the system. It handles all incoming search requests, manages the flow of data between services, applies caching logic, and aggregates results before presenting them to the user.
*   **Elasticsearch Cluster:** A distributed search and analytics engine. It comprises three nodes (`es01`, `es02`, `es03`) responsible for indexing documents and executing initial BM25-ranked searches. This setup ensures data redundancy and horizontal scalability.
*   **Flask Service (Python - Re-ranking):** A dedicated microservice implemented in Python using Flask. It hosts a pre-trained DistilBERT model (or similar SentenceTransformers model) and exposes an API endpoint (`/re-rank`) to perform semantic re-ranking of search results. This service is computationally intensive and runs independently.
*   **Redis:** An in-memory data structure store used as a high-speed cache. It stores frequently requested search results and potentially user interaction data to accelerate subsequent queries and personalize results.

## Services Overview (Spring Boot Application)

The Spring Boot application encapsulates several key services:

*   **SearchService:** Responsible for constructing and executing dynamic Elasticsearch queries. It supports various query types, from simple keyword matching to complex pattern and proximity searches.
*   **BM25RankingService:** Interacts directly with the Elasticsearch cluster to retrieve an initial list of documents, ranked by their BM25 scores. This provides a baseline relevance ranking.
*   **ReRankingService:** Acts as a client to the external Flask service. It sends the initial BM25-ranked results and the original query to the Flask service, which then returns a semantically re-ranked list.
*   **InteractionsReRankingService:** (Conceptual/Future Enhancement) This service would enhance ranking by combining the re-ranked search results with user interaction data (e.g., click-through rates, viewing history) stored in Redis, providing a more personalized search experience.
*   **RedisCacheService:** Manages all caching operations, including storing search results, validating cache consistency, and clearing outdated entries to ensure data freshness.

## Technology Stack

*   **Backend Framework:** Spring Boot (Java)
*   **Search & Indexing:** Elasticsearch
*   **Caching:** Redis
*   **Semantic Re-ranking:** DistilBERT, SentenceTransformers (via Python Flask service)
*   **ML Service Framework:** Flask (Python)
*   **Containerization:** Docker

## Project Structure and Files

```
apis-searching-in-distributed-system/
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml                                 # Maven Project Object Model for Spring Boot application
├── README.md                               # This file
├── demo/                                   # Contains demonstration-related files (e.g., example data, scripts)
│   ├── .mvn/
│   ├── elasticsearch-setup/                # Scripts/configurations for setting up Elasticsearch
│   ├── src/
│   └── ... (other Spring Boot related files for demo)
└── reranking_service/                      # Python Flask service for DistilBERT re-ranking
    ├── Dockerfile                          # Dockerfile for containerizing the Flask service
    ├── app.py                              # Flask application for the re-ranking API
    └── requirements.txt                    # Python dependencies for the Flask service
```

## Getting Started

To set up and run this distributed search system locally, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/amgharhind/apis-searching-in-distributed-system.git
    cd apis-searching-in-distributed-system
    ```
2.  **Set up Elasticsearch:**
    *   Navigate to the `demo/elasticsearch-setup` directory (or equivalent) and follow the instructions to set up a local Elasticsearch cluster (e.g., using Docker Compose).
    *   Ensure Elasticsearch is running and accessible.
3.  **Set up Redis:**
    *   Run a Redis instance, preferably using Docker:
        ```bash
        docker run --name some-redis -p 6379:6379 -d redis
        ```
4.  **Build and Run the Re-ranking Flask Service:**
    *   Navigate to the `reranking_service` directory.
    *   Build the Docker image:
        ```bash
        docker build -t reranking-service .
        ```
    *   Run the container:
        ```bash
        docker run -p 5000:5000 --name reranker -d reranking-service
        ```
    *   (Ensure port 5000 is available or adjust as needed).
5.  **Build and Run the Spring Boot Application:**
    *   Navigate to the root directory of the project.
    *   Build the Spring Boot application using Maven:
        ```bash
        ./mvnw clean install
        ```
    *   Run the application:
        ```bash
        java -jar target/apis-searching-in-distributed-system-0.0.1-SNAPSHOT.jar # Adjust JAR name as necessary
        ```
    *   Alternatively, run from your IDE.

## Expected Results and Performance

This system is designed to provide:

*   **High Relevance:** The two-stage re-ranking with DistilBERT significantly improves the semantic relevance of search results compared to traditional keyword-based approaches.
*   **Low Latency:** Redis caching ensures that frequently requested queries are served almost instantly, providing a smooth user experience.
*   **Scalability:** The distributed nature of Elasticsearch and the containerized microservices allow for easy scaling to handle increased data volumes and query loads.
*   **Fault Tolerance:** The Elasticsearch cluster provides data redundancy and ensures the search system remains operational even if individual nodes fail.

## Limitations and Future Work

*   **Model Complexity:** While DistilBERT offers a good balance of performance and speed, more complex models could potentially yield even higher semantic accuracy, albeit with increased computational cost.
*   **Real-time Indexing:** The current setup focuses on search. Implementing a robust real-time indexing pipeline for new documents would be a valuable enhancement.
*   **User Interaction Feedback Loop:** While `InteractionsReRankingService` is mentioned, a full implementation of a feedback loop where user interactions (clicks, dwell time) continuously refine the ranking models would further personalize and improve results.
*   **Query Understanding:** Advanced natural language understanding for complex user queries (e.g., conversational search) could be integrated to provide more intuitive search experiences.
*   **Monitoring and Alerting:** Implementing comprehensive monitoring and alerting for all microservices and the Elasticsearch cluster is crucial for production environments.

## Examples of Usage

Once the system is running, you can interact with the search API exposed by the Spring Boot application. For example, a search query for "machine learning algorithms" might return documents related to various ML techniques, with the DistilBERT re-ranking ensuring that the most contextually relevant documents appear first.

## License

This project is open-source and available under the [MIT License](LICENSE).

