rm -rf ./src/main/resources/static/lib
yarn install
cp -r ./node_modules ./src/main/resources/static/lib
rm -rf ./node_modules

