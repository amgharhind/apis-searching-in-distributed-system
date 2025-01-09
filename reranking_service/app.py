from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util
from elasticsearch import Elasticsearch

app = Flask(__name__)

# Use DistilBERT for  re-ranking
MODEL_NAME = 'sentence-transformers/distilbert-base-nli-stsb-mean-tokens'
print("Loading model:", MODEL_NAME)
model = SentenceTransformer(MODEL_NAME)

# Connect to Elasticsearch cluster (7.12)
es = Elasticsearch(["http://es01:9200", "http://es02:9201", "http://es03:9202"])

@app.route('/re-rank', methods=['POST'])
def re_rank():
    try:
        # Input: query, Elasticsearch index, and number of results to re-rank
        data = request.json
        query = data['query']
        index = data['index']
        size = data.get('size', 10)  # Default to 10 results

        # Step 1: Retrieve top N documents from Elasticsearch using BM25
        search_response = es.search(index=index, body={
            "query": {
                "match": {
                    "content": query
                }
            },
            "size": size
        })

        # Extract document IDs and content
        documents = []
        document_ids = []
        for hit in search_response['hits']['hits']:
            documents.append(hit['_source']['content'])
            document_ids.append(hit['_id'])

        # Step 2: Compute semantic similarity using DistilBERT
        query_embedding = model.encode(query, convert_to_tensor=True)
        doc_embeddings = model.encode(documents, convert_to_tensor=True, batch_size=4)

        # Compute cosine similarity
        similarities = util.cos_sim(query_embedding, doc_embeddings)[0]

        # Step 3: Rank the documents based on similarity scores
        ranked_results = sorted(
            zip(document_ids, documents, similarities.tolist()),
            key=lambda x: -x[2]  # Sort by descending similarity score
        )

        # Prepare the response
        response = [{"id": doc[0], "content": doc[1], "score": doc[2]} for doc in ranked_results]
        return jsonify({"ranked_results": response})

    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "Welcome to the Re-Ranking Service! Use the /re-rank endpoint."}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
