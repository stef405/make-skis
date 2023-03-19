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

def deletepuzzle(request):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    json_data = json.loads(request.body)
    user_id = json_data['user_id']
    puzzle_id = json_data['puzzle_id']

    cursor = connection.cursor()
    # if puzzle_id doesn't exist for user_id return 404
    cursor.execute('SELECT * FROM puzzles WHERE (user_id = '
                   '(%d) AND puzzle_id = (%d);', (user_id, puzzle_id))
    if (cursor.fetchone() == None):
        return HttpResponse(status=404)
    
    cursor.execute('DELETE FROM puzzles WHERE (puzzle_id = '
                   '(%d) AND user_id = (%d);', (puzzle_id, user_id))

    return JsonResponse({})

def deletepuzzle(request):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    json_data = json.loads(request.body)
    user_id = json_data['user_id']
    puzzle_id = json_data['puzzle_id']

    cursor = connection.cursor()
    # if puzzle_id doesn't exist for user_id return 404
    cursor.execute('SELECT * FROM puzzles WHERE (user_id = '
                   '(%d) AND puzzle_id = (%d);', (user_id, puzzle_id))
    if (cursor.fetchone() == None):
        return HttpResponse(status=404)
    
    cursor.execute('DELETE FROM puzzles WHERE (puzzle_id = '
                   '(%d) AND user_id = (%d);', (puzzle_id, user_id))

    return HttpResponse(status=204)

@csrf_exempt
def postpuzzle(request):
    if request.method != 'POST':
        return HttpResponse(status=400)

    # loading multipart/form-data
    user_id = request.POST.get("user_id")
    puzzle_name = request.POST.get("puzzle_name")

    if request.FILES.get("puzzle_image"):
        content = request.FILES['puzzle_image']
        filename = user_id+str(time.time())+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        puzzle_imageurl = fs.url(filename)
    else:
        return HttpResponse(status=400)
        
    cursor = connection.cursor()
    cursor.execute('INSERT INTO puzzles (user_id, puzzle_name, puzzle_imageurl) VALUES '
                   '(%d, %s, %s);', (user_id, puzzle_name, puzzle_imageurl))

    return HttpResponse(status=201)

# Create your views here.
