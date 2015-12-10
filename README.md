# VanityGenerator
A Java implementation of Bitcoin address Vanity Generator

This project is inspired by <a href="https://bitcointalk.org/index.php?topic=25804.0">Vanitygen</a> that is built in Java and relies on <a href="https://github.com/bitcoinj/bitcoinj">bitcoinJ</a>.

What it does:
* Searches for queries supplied by the user in a single or multi-threaded approach.
* Can search for compressed, uncompressed addresses.
* Can search for P2SH addresses
* Provides simple stats while searching such as total amount of addresses generated since that search was started and speed per second.

Defining Queries:
* The basis of searching relies on `Pattern` provided by java.util.regex. `RegexQuery` is the base definition of a query; it is a wrapper around a `Pattern` and a few booleans indicating what type of address to search for.
* Another type of query is simply called `Query` which takes a `QueryBuilder` as its definition. `Query` is meant to serve as a more flexible `RegexQuery` such that you are able to define a word (query) and decide how you want it to show up in an address. With `Query` you are able to switch between a 'Begins' and a 'Contains' searching pattern, toggle case insensitivity, and you are provided with pseudo odds on how long it will take to find.
* The last type of query you can use is a `NetworkQuery` (which may be deprecated shortly - 12-9-15) which extends `RegexQuery`. Everything is the same as `RegexQuery` along with defining a `Network` to search on by itself. If you have a `Search` thread running with a predefined `GlobalNetParams` and you have one `NetworkQuery` whose network does not match the threads network, `NetworkQuery` will only use its own `Network` when `mathces` is called.

Coming Soon:
* Encrypted keys
* Better odds
* Much more

What needs work:
* Flexibility of searching with regular expressions (removing sub patterns if desired)
* Test classes

Examples are provided within the examples module.
