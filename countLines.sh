#!/bin/bash
ts1=`wc -l src/com/binroot/gpa/* | tail -1`
ts2=`wc -l src/misc/* | tail -1`
ts3=`wc -l src/model/* | tail -1`
ts4=`wc -l res/layout/* | tail -1`

t1=0
t2=0
t3=0
t4=0

export IFS=" "
for word in $ts1; do
    if ! [[ "$word" =~ ^[0-9]+$ ]] ; then
	false
	else t1="$word"
    fi
done
for word in $ts2; do
    if ! [[ "$word" =~ ^[0-9]+$ ]] ; then
	false
	else t2="$word"
    fi
done
for word in $ts3; do
    if ! [[ "$word" =~ ^[0-9]+$ ]] ; then
	false
	else t3="$word"
    fi
done
for word in $ts4; do
    if ! [[ "$word" =~ ^[0-9]+$ ]] ; then
	false
	else t4="$word"
    fi
done

echo -e "src/com/binroot/gpa/*\t $t1"
echo -e "src/misc/*\t\t $t2"
echo -e "src/model/*\t\t $t3"
echo -e "res/layout/*\t\t $t4"
tot=`expr $t1 + $t2 + $t3 + $t4`
echo $tot