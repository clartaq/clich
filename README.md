# Clich #

Clich is a very simple, small, in-browser [Rich Text](https://en.wikipedia.org/wiki/Rich_Text_Format) Editor written in [ClojureScript](https://clojurescript.org).

### Why? ###

I saw Jared Reich's [Pell](https://github.com/jaredreich/pell) editor on GitHub and was blown away by how much could be done with such a small program. Then, I thought "Too bad it's in JavaScript." So, I decided to see if I could write something similar in a nice language like ClojureScript.

### What's It Good For ###

Not much. It's a demo. I wouldn't try to write a novel with it, or even a blog post. But, if you need something to capture small notes quickly, it might do.

## Building ##

A [Leiningen](https://leiningen.org) script is included to build the project.

### Dependencies and Size ###

Clich is not free of dependencies like Pell. Clich uses the excellent [Reagent](https://github.com/reagent-project/reagent) libraray and, thus, pulls in [React](https://reactjs.org) too. The ClojureScript and CSS are just a few hundred lines, similar to Pell. But the additional dependencies swell the production build to about 4MB. 

### Development mode ###

To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.
Once Figwheel starts up, you should be able to open the `public/index.html` page in the browser.


### Building for production ###

```
lein clean
lein package
```

To run the demo, open the `public/index.html` file in the browser of your choice.