EXTRACT
======

EXTRACT makes it easy to extract and deliver of your geodata

EXTRACT imports the geodata orders placed on an easySDI portal, 
then executes a series of pre-configured tasks to extract the requested data,
and returns the result to the customer: with or without human intervention, you define it!

By automating the process of extracting and delivering your geodata, you:
* reduce order processing times,
* increase the quality of the data delivered,
* increase customer satisfaction. 

#### Prerequisites:
* Windows or Linux
* Java 7 OR Java 8 (Java 9+ not supported), Oracle OR OpenJDK
* Tomcat 7 to 9
* PostgreSQL >= 9.4

#### Try it (with docker):
```bash
mvn clean install
docker-compose up
```

Open `http://localhost:8080/extract/login` in your browser,\
default user is `admin` with password `motdepasse21`.

Now read the doc 😊

#### Doc:

About page (FR) : https://www.asitvd.ch/partager/automatiser-la-diffusion-des-geodonnees-avec-extract.html

Help & doc page (FR) : https://projets.asitvd.ch/projects/extracteur/wiki

Forked from easySDI (www.easysdi.org) : https://svn.easysdi.org/svn/easysdi/branches/4.5.x/java
