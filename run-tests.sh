#!/bin/bash
#
# Script pour lancer les tests Extract avec Docker (sans Maven/JDK local)
# Usage: ./run-tests.sh [unit|integration|all|specific]
#
# Pré-requis: Docker et Docker Compose installés
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default test type
TEST_TYPE=${1:-unit}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}     Extract Test Runner (Docker)${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is required but not installed${NC}"
    echo "Please install Docker from https://docs.docker.com/get-docker/"
    exit 1
fi

echo -e "${GREEN}✓ Docker detected${NC}"

# Function to build project in Docker
build_project() {
    local verbose=${1:-false}
    echo -e "${YELLOW}Building project with Docker (without tests)...${NC}"
    
    # Determine verbosity
    local quiet_flag="--quiet"
    if [ "$verbose" = true ]; then
        quiet_flag=""
        echo -e "${BLUE}Verbose mode: showing build output${NC}"
    fi
    
    # First build the parent project to resolve dependencies
    docker run --rm \
        -v "$PWD:/workspace" \
        -v "$HOME/.m2:/root/.m2" \
        -w /workspace \
        maven:3.8-openjdk-17 \
        mvn clean install -DskipTests -Dmaven.test.skip=true $quiet_flag
    
    echo -e "${GREEN}✓ Build completed (no tests run)${NC}"
}

# Function to run tests in Docker
run_tests_docker() {
    local test_command=$1
    local test_description=$2
    
    echo -e "${GREEN}Running ${test_description} in Docker...${NC}"
    
    docker run --rm \
        -v "$PWD:/workspace" \
        -v "$HOME/.m2:/root/.m2" \
        -w /workspace/extract \
        --network host \
        maven:3.8-openjdk-17 \
        mvn ${test_command} --batch-mode
}

# Check if we need to build first
if [ ! -d "$HOME/.m2/repository/ch/asit_asso" ] || [ "$TEST_TYPE" = "build" ]; then
    build_project
fi

case "$TEST_TYPE" in
    build)
        echo -e "${GREEN}Project built successfully${NC}"
        ;;
    
    build-verbose)
        build_project true
        echo -e "${GREEN}Project built successfully (verbose mode)${NC}"
        ;;
        
    unit)
        echo -e "${GREEN}Test type: UNIT TESTS${NC}"
        echo ""
        # Run unit tests using the unit-tests profile
        echo -e "${YELLOW}Running unit tests...${NC}"
        run_tests_docker "test -Punit-tests" "unit tests"
        ;;
        
    integration)
        echo -e "${GREEN}Test type: INTEGRATION TESTS${NC}"
        echo -e "${YELLOW}Starting required services with docker-compose...${NC}"
        
        # Start services for integration tests
        docker compose -f docker-compose-test.yaml up -d pgsql mailhog openldap ldap-ad
        
        # Wait for services to be ready
        echo -e "${YELLOW}Waiting for services to be ready...${NC}"
        sleep 10
        
        # Run all tests (unit + integration) in Docker with services available
        docker run --rm \
            -v "$PWD:/workspace" \
            -v "$HOME/.m2:/root/.m2" \
            -w /workspace/extract \
            --network extract_default \
            maven:3.8-openjdk-17 \
            mvn verify -DskipTests=false --batch-mode \
                -Dspring.datasource.url=jdbc:postgresql://pgsql:5432/extract \
                -Dspring.datasource.username=extractuser \
                -Dspring.datasource.password=demopassword \
                -Dspring.profiles.active=test
        
        # Stop services
        echo -e "${YELLOW}Stopping services...${NC}"
        docker compose -f docker-compose-test.yaml down
        ;;
        
    all)
        echo -e "${GREEN}Test type: ALL TESTS${NC}"
        echo ""
        
        # Start services for all tests
        echo -e "${YELLOW}Starting services...${NC}"
        docker compose -f docker-compose-test.yaml up -d pgsql mailhog openldap ldap-ad
        sleep 10
        
        # Run all tests with services
        docker run --rm \
            -v "$PWD:/workspace" \
            -v "$HOME/.m2:/root/.m2" \
            -w /workspace/extract \
            --network extract_default \
            maven:3.8-openjdk-17 \
            mvn clean verify -DskipTests=false --batch-mode \
                -Dspring.datasource.url=jdbc:postgresql://pgsql:5432/extract \
                -Dspring.datasource.username=extractuser \
                -Dspring.datasource.password=demopassword \
                -Dspring.profiles.active=test
        
        docker compose -f docker-compose-test.yaml down
        ;;
        
    specific)
        # Run specific test class
        TEST_CLASS=${2:-RequestModelTest}
        echo -e "${GREEN}Running specific test: ${TEST_CLASS}${NC}"
        
        docker run --rm \
            -v "$PWD:/workspace" \
            -v "$HOME/.m2:/root/.m2" \
            -w /workspace/extract \
            maven:3.8-openjdk-17 \
            mvn test -Dtest=${TEST_CLASS} --batch-mode
        ;;
        
    clean)
        echo -e "${YELLOW}Cleaning Maven cache and rebuilding...${NC}"
        echo -e "${YELLOW}Removing Maven cache for ch.asit_asso...${NC}"
        
        # Use Docker to clean the cache with proper permissions
        docker run --rm \
            -v "$HOME/.m2:/root/.m2" \
            maven:3.8-openjdk-17 \
            bash -c "rm -rf /root/.m2/repository/ch/asit_asso"
        
        echo -e "${GREEN}✓ Cache cleaned${NC}"
        build_project true  # Use verbose mode for clean build
        ;;
        
    *)
        echo -e "${RED}Invalid test type: $TEST_TYPE${NC}"
        echo ""
        echo -e "${BLUE}Usage: $0 [build|build-verbose|unit|integration|all|specific|clean]${NC}"
        echo ""
        echo "Commands:"
        echo "  build                      # Build project only (quiet mode)"
        echo "  build-verbose              # Build project with full output"
        echo "  unit                       # Run unit tests only"
        echo "  integration                # Run integration tests with services"  
        echo "  all                        # Run all tests"
        echo "  specific TestClassName     # Run specific test class"
        echo "  clean                      # Clean cache and rebuild (verbose)"
        echo ""
        echo "Examples:"
        echo "  $0                         # Run unit tests (default)"
        echo "  $0 build                   # Build project quietly"
        echo "  $0 clean                   # Clean and rebuild from scratch"
        echo "  $0 unit                    # Run unit tests"
        echo "  $0 integration             # Run integration tests"
        echo "  $0 specific RequestModelTest # Run specific test"
        echo ""
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}     ✓ Completed successfully!${NC}"
echo -e "${BLUE}========================================${NC}"