from django.db import models

# Create your models here.

class Puzzle(models.Model):
    puzzle_id = models.IntegerField
    user_id = models.IntegerField
    puzzle_img = models.URLField
    piece_ct = models.IntegerField
    width = models.IntegerField
    height = models.IntegerField

class Piece(models.Model):
    piece_id = models.IntegerField
    piece_img = models.CharField(max_length=50)
    solution_img = models.URLField
    puzzle_id = models.ForeignKey(Puzzle, on_delete=models.CASCADE)
    difficulty = models.IntegerField