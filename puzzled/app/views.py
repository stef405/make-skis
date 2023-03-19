from django.shortcuts import render


from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
import json


import os, time
from django.conf import settings
from django.core.files.storage import FileSystemStorage

@csrf_exempt
def postimages(request):
    if request.method != 'POST':
        return HttpResponse(status=400)

    # loading multipart/form-data
    username = request.POST.get("username")
    message = request.POST.get("message")

    if request.FILES.get("image"):
        content = request.FILES['image']
        filename = username+str(time.time())+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        imageurl = fs.url(filename)
    else:
        imageurl = None

    if request.FILES.get("video"):
        content = request.FILES['video']
        filename = username+str(time.time())+".mp4"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        videourl = fs.url(filename)
    else:
        videourl = None
        
    cursor = connection.cursor()
    cursor.execute('INSERT INTO chatts (username, message, imageurl, videourl) VALUES '
                   '(%s, %s, %s, %s);', (username, message, imageurl, videourl))

    return JsonResponse({})



def getchatts(request):
    if request.method != 'GET':
        return HttpResponse(status=404)

    cursor = connection.cursor()
    cursor.execute('SELECT username, message, time FROM chatts ORDER BY time DESC;')
    rows = cursor.fetchall()

    response = {}
    response['chatts'] = rows
    return JsonResponse(response)

def getimages(request):
    if request.method != 'GET':
        return HttpResponse(status=404)

    cursor = connection.cursor()
    cursor.execute('SELECT username, message, time, imageurl, videourl FROM chatts ORDER BY time DESC;')
    rows = cursor.fetchall()

    response = {}
    response['chatts'] = rows
    return JsonResponse(response)


@csrf_exempt
def postchatt(request):
    if request.method != 'POST':
        return HttpResponse(status=404)

    json_data = json.loads(request.body)
    username = json_data['username']
    message = json_data['message']

    cursor = connection.cursor()
    cursor.execute('INSERT INTO chatts (username, message) VALUES '
                   '(%s, %s);', (username, message))

    return JsonResponse({})

@csrf_exempt
def deletepuzzle(request):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    json_data = json.loads(request.body)
    puzzle_id = json_data['puzzle_id']

    cursor = connection.cursor()
    cursor.execute('DELETE FROM puzzles WHERE puzzle_id = '
                   '(%d);', puzzle_id)

    return JsonResponse({})


# Create your views here.
