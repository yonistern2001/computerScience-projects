const express = require("express");
const app = express();

const mongo= require("./mongo_util.js");

app.use(express.urlencoded({extended: true}));

app.get("/", (_, res) => {
    res.sendFile(__dirname + "/public/index.html");
});

app.get("/contact", (_, res) => {
    res.sendFile(__dirname + "/public/add_subscribers.html");
});

app.get("/age-filter", (_, res) => {
    res.sendFile(__dirname + "/public/age_filter.html");
});

app.get("/subscribers", (_, res) => {
    mongo.getSubscribers().then((value) => {
        res.send("<pre style=\"font-size: 18px\">" + value + "</pre>");
    }).catch(error => handleError(error, res));
});

app.post("/subscribe", (req, res) => {
    const name= req.body.name;
    const email= req.body.email;
    const zipcode= req.body.zipCode;
    const age= req.body.age;

    mongo.addSubscriber(name, email, zipcode, age).then((_) => {
        res.send("Subscriber added");
    }).catch(error => handleError(error, res));
});

app.post("/process-age-filter", (req, res) => {
    const age= req.body.agefilter;

    mongo.deleteByAge(age).then((value) => {
        res.send("Deleted " + value["deletedCount"] + " subscribers");
    }).catch(error => handleError(error, res));
});

app.use((_, res) => {
    res.status(404).send("URL Not Found");
})

app.listen(3000, () => {
    console.log("Listening on port 3000");
})

function handleError(error, res) {
    if(error instanceof mongo.ValidationError) {
        res.status(400).send("Validation error: " + error.message);
    } else {
        res.status(500).send("Server-side error: " + error.message);
    }
}