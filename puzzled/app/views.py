from django.shortcuts import render


from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
import json


import os, time
from django.conf import settings
from django.core.files.storage import FileSystemStorage
from .all_functions import *

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
def deletepuzzle(request, puzzle_id):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    cursor = connection.cursor()
    # if puzzle_id doesn't exist for user_id return 404
    cursor.execute('SELECT * FROM puzzles WHERE puzzle_id = %s;', (puzzle_id, ))
    if (cursor.fetchone() == None):
        return HttpResponse(status=404)
    
    cursor.execute('DELETE FROM puzzles WHERE puzzle_id = %s;', (puzzle_id, ))

    return HttpResponse(status=204)

def getpuzzles(request, user_id):
    if request.method != 'GET':
        return HttpResponse(status=404)
    
    # user_id = int(request.GET['user_id'])
    
    cursor = connection.cursor()
    cursor.execute("""SELECT * FROM puzzles WHERE (user_id = %s);""", (user_id, ))
    rows = cursor.fetchall()


    response = {'puzzles': []}
    for row in rows:
        row = list(row)
        puzzle = {}
        puzzle['puzzle_id'] = row[0]
        puzzle['puzzle_img'] = row[2]
        puzzle['width'] = row[3]
        puzzle['height'] = row[4]
        response['puzzles'].append(puzzle)
    return JsonResponse(response)

def getpieces(request, puzzle_id):
    if request.method != 'GET':
        return HttpResponse(status=404)
    
    cursor = connection.cursor()
    cursor.execute("""SELECT * FROM pieces WHERE puzzle_id = %s;""", (puzzle_id, ))
    rows = cursor.fetchall()

    response = {'pieces': []}
    for row in rows:
        row = list(row)
        piece = {}
        piece['piece_id'] = row[0]
        piece['piece_img'] = row[1]
        piece['solution_img'] = row[2]
        piece['difficulty'] = row[4]
        piece['width'] = row[5]
        piece['height'] = row[6]
        response['pieces'].append(piece)
    return JsonResponse(response)

@csrf_exempt
def postpuzzle(request):
    if request.method != 'POST':
        return HttpResponse(status=400)
    # loading multipart/form-data
    user_id = request.POST.get("user_id")
    width = request.POST.get('width')
    height = request.POST.get('height')
    
    if request.FILES.get("puzzle_img"):
        content = request.FILES['puzzle_img']
        filename = user_id+str(time.time())+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        puzzle_image_url = fs.url(filename)
    else:
        return HttpResponse(status=400)
    
    if is_blurry(content):
        return HttpResponse(status=202)

    cursor = connection.cursor()
    cursor.execute('INSERT INTO puzzles (user_id, puzzle_img, width, height) VALUES '
                   '(%s, %s, %s, %s);', (user_id, puzzle_image_url, width, height))

    return HttpResponse(status=201)

@csrf_exempt
def postpiece(request):
    if request.method != 'POST':
        return HttpResponse(status=400)

    # loading multipart/form-data
    puzzle_id = request.POST.get("puzzle_id")
    difficulty = request.POST.get('difficulty')
    width = request.POST.get('width')
    height = request.POST.get('height')
    

    if request.FILES.get("piece_img"):
        content = request.FILES['piece_img']
        filename = puzzle_id+str(time.time())+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        piece_image_url = fs.url(filename)
    else:
        return HttpResponse(status=400)
        
    cursor = connection.cursor()
    # TODO: Replace the insert for solution_img with the actual solution once we have a
    # way of generating it
    cursor.execute('INSERT INTO pieces (puzzle_id, piece_img, difficulty, solution_img, width, height) VALUES '
                   '(%s, %s, %s, %s, %s, %s);',
                    (puzzle_id, piece_image_url, difficulty, piece_image_url, width, height))

    return HttpResponse(status=201)

@csrf_exempt
def deletepiece(request, piece_id):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    cursor = connection.cursor()
    # if puzzle_id doesn't exist for user_id return 404
    cursor.execute('SELECT * FROM pieces WHERE piece_id = %s;', (piece_id, ))
    if (cursor.fetchone() == None):
        return HttpResponse(status=404)
    
    cursor.execute('DELETE FROM pieces WHERE piece_id = %s;', (piece_id, ))

    return HttpResponse(status=204)

# Create your views here.
