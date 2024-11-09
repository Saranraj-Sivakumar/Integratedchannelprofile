function search() {
    // Get the query from the input field, replace spaces with "+" for URL formatting, then encode
    const query = document.getElementById('searchQuery').value.replace(/\s+/g, "+");
    const encodedQuery = encodeURIComponent(query);

    // Check for empty query
    if (!query.trim()) {
        alert("Please enter a search query.");
        return;
    }

    fetch(`/tubelytics/search?query=${encodedQuery}`)
        .then(response => response.json())
        .then(data => {
            let resultsDiv = document.getElementById("results");

            // Retrieve and display the average readability scores and sentiment from the response
            let avgFkGrade = data.avgFleschKincaidGrade || "##";
            let avgReadingEase = data.avgFleschReadingEase || "##";
            let avgSentiment = data.averageSentiment || ":-|"; // Default to neutral if no sentiment data

            // Create a new query result container at the top of the results
            let queryDiv = document.createElement("div");
            queryDiv.className = "search-header";
            queryDiv.innerHTML = `
                <p>Search terms: ${query} <span class="sentiment">${avgSentiment}</span> (Flesch-Kincaid Grade Level Avg. = ${avgFkGrade}, Flesch Reading Ease Score Avg. = ${avgReadingEase}) <a href="#" class="more-stats"> More stats</a></p>
            `;

            // Append individual video results to this new query container
            data.items.forEach((item, index) => {
                let fkGrade = item.snippet.fkGrade || "##";
                let readingEase = item.snippet.readingEase || "##";
                let sentiment = item.sentiment || ":-|"; // Assuming the sentiment is part of each item in the response

                let video = document.createElement("div");
                video.className = "video-result";
                video.innerHTML = `
                    <div class="video-content">
                        <p class="video-title">${index + 1}. Title: <a href="https://www.youtube.com/watch?v=${item.id.videoId}" target="_blank">${item.snippet.title}</a></p>
                        <p><strong>Channel:</strong> <a href="/channel/${item.snippet.channelId}">${item.snippet.channelTitle}</a></p>
                        <p><strong>Description:</strong> "${item.snippet.description}"</p>
                        <p class="readability-score">Flesch-Kincaid Grade Level = ${fkGrade}, Flesch Reading Ease Score = ${readingEase}</p>
                        <p class="sentiment">Sentiment: ${sentiment}</p>
                        <p><a href="#" class="tags-link">Tags</a></p>
                    </div>
                    <div class="video-thumbnail">
                        <img src="${item.snippet.thumbnails.default.url}" alt="Thumbnail" class="thumbnail">
                    </div>
                `;
                queryDiv.appendChild(video);
            });

            // Insert the new query results at the top of the results div
            resultsDiv.prepend(queryDiv);

            // Keep only the most recent 10 search queries
            if (resultsDiv.children.length > 10) {
                let excessResults = resultsDiv.children.length - 10;
                for (let i = 0; i < excessResults; i++) {
                    resultsDiv.removeChild(resultsDiv.lastChild);
                }
            }
        })
        .catch(error => {
            console.error("Error fetching search results:", error);
            let resultsDiv = document.getElementById("results");
            resultsDiv.innerHTML = "<p>Something went wrong, please try again later.</p>";
        });
}
