#!/bin/bash
#
# creates the python classes for our .proto
#

project_base="/Users/lingzhang/Documents/javaworkspace/cmpe275project1CaoNew_CpForProtobuf3"


#rm ${project_base}/src/image_pb2.py

protoc -I=${project_base}/resources --python_out=${project_base}/python ${project_base}/resources/Image.proto 
