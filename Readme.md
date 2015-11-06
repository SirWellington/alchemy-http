Alchemy HTTP
==============================================

<img src="https://raw.githubusercontent.com/SirWellington/alchemy/develop/Graphics/Logo/Alchemy-Logo-v3-name.png" width="200">

## "REST without the MESS"

[![Build Status](https://travis-ci.org/SirWellington/alchemy-http.svg)](https://travis-ci.org/SirWellington/alchemy-http)

# Purpose
Why can't making a REST call in Java be as easy and fluid as it is for other languages?

Making REST calls in other languages is **FAR SIMPLER!**

### Javascript
In JavaScript:

```javascript
$.ajax({
	   url: "http://aroma.coffee/placeOrder",
	   type: 'POST',
	   data: { "size": "large",
	   		"type": "black",
	   		"amount": 3
		}
   }).then(function(data) {
	  var orderNumber = data.orderNumber;
   });
```

### Ruby
In Ruby:

```ruby
uri = URI('http://aroma.coffee/orders?orderNumber=99')
Net::HTTP.get(uri) # => String
```
### Java
Meanwhile, back in Java land...
```java

HttpClient apacheHttp = //Good luck figuring out how to create me

//Create the JSON Object, line by line...
JsonObject orderRequest = new JsonObject();
orderRequest.put("size", "large");
orderRequest.put("type", "black");
orderRequest.put("amount", 3);

//Set up the request method
HttpPost post = new HttpPost("http://aroma.coffee/orders");

//Stick the body in the body
Entity body = new StringEntity(orderRequest.toString(), "application/json");
post.setEntity(body);

//Execute the actual request
HttpResponse apacheResponse;
try
{
	apacheResponse = apacheHttp.execute(post);
}
catch(Exception ex)
{
	LOG.error("Oh god!", ex);
	throw new OperationFailedException(ex);
}

//Check the Response Code
if(apacheResponse.getStatusLine().getStatusCode() != 200)
{
	LOG.error("The Service didn't like our response");
	throw new OperationFailedException();
}

//Read the entity response
Entity responseEntity = apacheResponse.getEntity();
String responseString = null;
try (final InputStream istream = responseEntity.getContent())
{
	//If you're lucky enough to have Guava
 	byte[] rawBytes = ByteStreams.toByteArray(istream);
	responseString = new String(rawBytes, Charsets.UTF_8);
}
 catch (IOException ex)
{
	LOG.error("Failed to read entity from request", ex);
  	throw new RuntimeException(ex);
}

//Then you gotta parse the string
//Good luck with that one...

```
All that *just to get some damn JSON data*!

Come on Java! No wonder they hate us.
We can do better than that.

## The Alchemy Way
```java
AlchemyHttp http = AlchemyHttp.newDefaultInstance();

Coffee myCoffee = http.go()
					  .get()
					  .expecting(Coffee.class)
					  .at("http://aroma.coffee/orders?orderNumber=99");
//Wait...that's it?
```
**That's it!**

### The Async Way

There may be times when you don't care to wait for an immediate response from the service. An Async response would actually make a lot more sense.

> Just show me the damn code!

```java
http.go()
	.post()
	.body(request)
	.expecting(Coffee.class)
	.onSuccess(c -> LOG.error("What took you so long to get my cofee!: {}", c))
	.onFailure(ex -> LOG.error("What can I do without coffee?", ex))
	.at("http://aroma.coffee/orders");
```
#### Another way
To be fair Java Lambdas aren't as clean as `Blocks` in other languages.
It's often better to things somewhere else.

```java
class BaristaService
{
	private AlchemyHttp http;

	@Override
	public void serveCustomer(Customer customer)
	{
		http.go()
			.get()
			.expecting(Coffee.class)
			.onSuccess(coffee -> customer.accept(coffee))
			.onFailure(this::handleOrderIssue)
			.at("http://aroma.coffee/orders");
	}

	private void handleOrderIssue(AlchemyException ex)
	{
		HttpResponse response = ex.getResponse();
		LOG.error("What happened to our Coffee Machine? {} | {}" , response, response.statusCode());
		Map<String,String> responseHeaders = response.responseHeaders();
	}
}
```


# Download

To use, simply add the following maven dependency.

## Release
```xml
<dependency>
	<groupId>tech.sirwellington.alchemy</groupId>
	<artifactId>alchemy-http</artifactId>
	<version>1.0</version>
</dependency>
```
## Snapshot

```xml
<dependency>
	<groupId>tech.sirwellington.alchemy</groupId>
	<artifactId>alchemy-http</artifactId>
	<version>1.1-SNAPSHOT</version>
</dependency>
```
# [Javadocs](http://www.javadoc.io/doc/tech.sirwellington.alchemy/alchemy-http/)

# Tested Against
This library has been tested against in-house as well as production services.
This is not a comprehensive list, but should give you some idea on compatability.

## Google APIs

### Geocoding
https://maps.googleapis.com/maps/api/geocode/json

Documentation found [here](https://developers.google.com/maps/documentation/geocoding/get-api-key)

### Places API
https://maps.googleapis.com/maps/api/place/nearbysearch/json

Documentation found [here](https://developers.google.com/places/web-service/)

## Mashape APIs

Mashape has many great test APIs found [here](https://market.mashape.com).


# Requirements

+ Java 8
+ Maven installation


# Building
To build, just run a `mvn clean install` to compile and install to your local maven repository


# Feature Requests
Feature Requests are definitely welcomed! **Please drop a note in [Issues](https://github.com/SirWellington/alchemy-http/issues).**

# Release Notes

## 1.0
+ Initial Public Release

# License

This Software is licensed under the Apache 2.0 License

http://www.apache.org/licenses/LICENSE-2.0
