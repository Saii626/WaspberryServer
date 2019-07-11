#!/bin/bash

echo "Starting server upgrade"
SESSION_NAME="WaspberryServer"

tmux has-session -t ${SESSION_NAME}

if [ $? != 0 ]
then
    echo "Creating new session"
    tmux new -s $SESSION_NAME -d
else
    echo "Attaching to previous session"
    tmux attach -t $SESSION_NAME

    echo "Stopping old server"
    tmux send-keys -t $SESSION_NAME C-c
fi

cd "$(dirname "$0")"
serverFile=""
for file in $(pwd)/*
do
    if [[ $file == "WaspberryServer"* ]];
    then
        serverFile=$file
    fi
done

if [[ serverFile == "" ]];
then
    echo "No WaspberryServer found"
    exit 1
else
    echo "Starting new WaspberryServer" 
    tmux send-keys 'java -jar -Dspring.profiles.active=waspberry ' $file C-m
    tmux detach -s $SESSION_NAME
    echo "Done"
    exit 0
fi