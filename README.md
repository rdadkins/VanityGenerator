# VanityGenerator
A Java implementation of Bitcoin address Vanity Generator

This project is inspired by <a href="https://bitcointalk.org/index.php?topic=25804.0">Vanitygen</a> but built in Java that relies on <a href="https://github.com/bitcoinj/bitcoinj">bitcoinJ</a>.

What it does:
* Searches for queries supplied by the user in a single or multi-threaded approach.
* Allows for queries to be searched accross different networks other than Bitcoin.
* Allows for queries to be defined by regular expressions.
* Can search for compressed, uncompressed addresses.
* Provides simple stats while searching such as total amount of addresses generated since that search was started and speed per second.

What needs work:
* Ability to search for P2SH addresses.
* Flexibility of searching with regular expressions (removing sub patterns if desired)
* More.

Examples are provided within the examples module.
