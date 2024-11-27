From node:16
RUN mkdir /opt/i27
COPY . /opt/i27
CMD ["sleep", "3600"]
//copy these dockerfile on clothing-code 
// docker build -t node:cloth
// npm install
// npm start dev

