FROM python:3.11-slim

WORKDIR /app

# Install Flask
RUN pip install --no-cache-dir flask

# Copy the mock server script
COPY fme_server_mock.py .

# Expose port
EXPOSE 8888

# Run the server
CMD ["python", "fme_server_mock.py", "--port", "8888", "--host", "0.0.0.0"]
