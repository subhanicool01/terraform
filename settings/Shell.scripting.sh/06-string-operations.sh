#!/bin/bash

myVar= "name of the collage in hyd"

myVarlength=${#myVar}

echo "length of the var is $myVarlength"


myBranch= "how many branches are in repo"

myRepoBranch=${#myBranch}

echo " how many branches in my all repo $myRepoBrnch"



#use for upper case and lower cases

echo "upper case of my varaiable is ${myVar^^}"

echo "lower case of my varaible is ${myVar,,}"


# how to replace my string key

replace=${myVar/collage/school}

