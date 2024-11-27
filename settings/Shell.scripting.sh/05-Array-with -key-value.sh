#!/bin/bash

declare -A myDetails

myDetails= ( [name]=subhani [age]=27 [city]=hyd )

echo "my name is "${myDetails[name]}" and age is "${myDetails[age]}"







declare -A myBio

myBio= ( [name]=maha [age]=27 [city=vij] )

echo "my name is "${myBio[name]}" and age is "${myBio[age]}"
