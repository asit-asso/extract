FROM python:3.11-slim

WORKDIR /app

# Install Flask
RUN pip install --no-cache-dir flask

# Copy the mock server script
COPY qgis_server_mock.py .

# Expose port
EXPOSE 8889

# Run the server
CMD ["python", "qgis_server_mock.py", "--port", "8889", "--host", "0.0.0.0"]
