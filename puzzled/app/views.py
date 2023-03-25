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
def deletepuzzle(request, puzzle_id):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    cursor = connection.cursor()
    # if puzzle_id doesn't exist for user_id return 404
    cursor.execute('SELECT * FROM puzzles WHERE puzzle_id = %s;', (puzzle_id,))
    if (cursor.fetchone() == None):
        return HttpResponse(status=404)
    
    cursor.execute('DELETE FROM puzzles WHERE puzzle_id = %s;', (puzzle_id,))

    return HttpResponse(status=204)

def getpuzzles(request, user_id):
    if request.method != 'GET':
        return HttpResponse(status=404)
    
    # user_id = int(request.GET['user_id'])
    
    cursor = connection.cursor()
    cursor.execute('SELECT * FROM puzzles WHERE (user_id = '
                   '?);', user_id)
    rows = cursor.fetchall()


    response = {'puzzles': []}
    for row in rows:
        row = list(row)
        puzzle = {}
        puzzle['puzzle_id'] = row[0]
        puzzle['puzzle_img'] = row[2]
        puzzle['piece_ct'] = row[3]
        puzzle['width'] = row[4]
        puzzle['height'] = row[5]
        response['puzzles'].append(puzzle)
    return JsonResponse(response)

def getpieces(request):
    if request.method != 'GET':
        return HttpResponse(status=404)

    user_id = request.GET['user_id']
    puzzle_id = request.GET['puzzle_id']
    
    cursor = connection.cursor()
    cursor.execute('SELECT * FROM pieces WHERE (user_id = '
                   '? AND puzzle_id = ?);', (user_id, puzzle_id))
    rows = cursor.fetchall()

    response = {}
    for row in rows:
        piece = {}
        piece['piece_id'] = row.get(0)
        piece['piece_img'] = row.get(1)
        piece['solution_img'] = row.get(2)
        piece['difficulty'] = row.get(4)
        response['pieces'].append(piece)
    return JsonResponse(response)

@csrf_exempt
def postpuzzle(request):
    if request.method != 'POST':
        return HttpResponse(status=400)
    # loading multipart/form-data
    user_id = request.POST.get("user_id")
    piece_ct = request.POST.get('piece_ct')
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
    
    cursor = connection.cursor()
    cursor.execute('INSERT INTO puzzles (user_id, puzzle_img, piece_ct, width, height) VALUES '
                   '(%s, %s, %s, %s, %s);', (user_id, puzzle_image_url, piece_ct, width, height))

    return HttpResponse(status=201)

@csrf_exempt
def postpiece(request):
    if request.method != 'POST':
        return HttpResponse(status=400)

    # loading multipart/form-data
    user_id = request.POST.get("user_id")
    puzzle_id = request.POST.get("puzzle_id")
    difficulty = request.POST.get('difficulty')
    

    if request.FILES.get("piece_img"):
        content = request.FILES['piece_img']
        filename = puzzle_id+str(time.time())+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        piece_image_url = fs.url(filename)
    else:
        return HttpResponse(status=400)
        
    cursor = connection.cursor()
    cursor.execute('INSERT INTO puzzles (user_id, puzzle_id, piece_image_url, difficulty) VALUES '
                   '(%s, %s, %s, %s);', (user_id, puzzle_id, piece_image_url, difficulty))

    return HttpResponse(status=201)

def deletepiece(request):
    # not sure if this should be DELETE
    if request.method != 'DELETE':
        return HttpResponse(status=404)

    user_id = request.DELETE.get('user_id')
    puzzle_id = request.DELETE.get('puzzle_id')
    piece_id = request.DELETE.get('piece_id')

    cursor = connection.cursor()
    # if puzzle_id doesn't exist for user_id return 404
    cursor.execute('SELECT * FROM puzzles WHERE (user_id = '
                   '(%s) AND puzzle_id = (%s) AND piece_id = (%s));', (user_id, puzzle_id, piece_id))
    if (cursor.fetchone() == None):
        return HttpResponse(status=404)
    
    cursor.execute('DELETE FROM puzzles WHERE (puzzle_id = '
                   '(%s) AND puzzle_id = (%s) AND piece_id = (%s));', (user_id, puzzle_id, piece_id))

    return HttpResponse(status=204)

# Create your views here.
