"""routing URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.1/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from app import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('getchatts/', views.getchatts, name='getchatts'),
    path('postchatt/', views.postchatt, name='postchatt'),
    path('getimages/', views.getimages, name='getimages'),
    path('postimages/', views.postimages, name='postimages'),
    path('deletepuzzle/', views.deletepuzzle, name='deletepuzzle'),
    path('postpuzzle/', views.postpuzzle, name='postpuzzle'),
    path('deletepiece/', views.deletepiece, name='deletepiece'),
    path('postpuzzle/', views.postpiece, name='postpiece'),
    path('getpuzzles/', views.getpuzzles, name='getpuzzles'),
]
