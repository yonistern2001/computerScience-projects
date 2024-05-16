const mongoose= require('mongoose');
mongoose.connect("mongodb://localhost:27017/NodeAndMongo3_db");
const ValidationError= mongoose.Error.ValidationError;

const userSchema= mongoose.Schema({
    name: {
        type: String,
        require: true,
        minlength: 1
    },
    email: {
        type: String,
        require: true,
        minlength: 1
    },
    zipCode: {
        type: String,
        require: true,
        minlength: 1
    },
    age: {
        type: Number,
        min: 1,
        max: 100,
        require: false
    }
},
{
    versionKey: false
});

User= mongoose.model("subscribers", userSchema);

const deleteByAge= async function(age) {
    return User.deleteMany({"age": {$gte: age}});
}

const addSubscriber= async function(name, email, zipcode, age) {
    const fields= {"name": name, "email": email, "zipCode": zipcode};
    if(age != "") {
        fields["age"]= age;
    }

    const subscriber= new User(fields);
    return subscriber.save();
}

const getSubscribers= async function() {
    return User.find({}, {_id: 0}).then((value) => {
        return JSON.stringify(value, null, "\t");
    });
}

module.exports= {
    deleteByAge, addSubscriber, getSubscribers, ValidationError
};