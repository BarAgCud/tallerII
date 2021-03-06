from flask import Flask,request,jsonify
from flask_restful import Resource, Api
import random

app = Flask(__name__)
api = Api(app)


password = ''
ide = ''
nombre = ''
token = 'magic'
usertest = 'test'

class login(Resource):
	def post(self):
		root = request.get_json(force=True)
		if root.get('id', '') == ide and root.get('password', '') == password:
			return { "token": token }, 201
		else:
			return { "error": "clave o usuario incorrecto" } , 401

class usuarios(Resource):
	def get(self):
		if request.args.get('r_user', '') ==  ide and request.args.get('token', '') == token:
			return  { "nuevobroadcast" : False, "usuarios": [ { "id": "pedro", "nombre": "Pedro Gomez", "estado" : "conectado", "nuevomsg": "true", "foto" : "" }, {"id": "juan", "nombre": "Juan Perez", "estado" : "conectado", "nuevomsg": "false", "foto" : ""}, {"id": "carlos", "nombre": "Carlos Johnson", "estado" : "desconectado", "nuevomsg": "false", "foto" : ""} ] } , 200
		else:
			return { "error": "token invalido" }, 401


class usuario(Resource):
	def get(self, user):
		if request.args.get('token', '') != token:
			return { "error": "token invalido" }, 401
		else:
			return { "nombre": "Usuarioprueba", "foto" : " ", "ubicacion": "Carlos Francisco Melo 5034, Villa Martelli, Buenos Aires, Argentina", "telefono" : "46345455", "email" : "pepefake", "ultimoacceso" : 1433342925 }, 200
			
	def post(self, user):
		root = request.get_json(force=True)
		global ide
		ide = user
		global nombre
		nombre = root['nombre']
		global password
		password = root['password']
		print "registrado usuario nuevo: " + ide
		return '', 201
	def put(self, user):
		root = request.get_json(force=True)
		if request.args.get('token', '') == token and request.args.get('r_user', '') ==  ide:
			global password
			password = root['password']
			print "nuevo password: " + password	
			global nombre
			nombre = root['nombre']
			print "nuevo nombre: " + nombre	
			return '', 201
		else:
			return {"error": "token invalido no existe el usuario"}, 401

class conversacion(Resource):
	def get(self, user):
		if request.args.get('token', '') == token and request.args.get('r_user', '') ==  ide:
			return [ { "id" : "test", "time" : 1432945648, "msg" : "hola" }, { "id" : "pepe", "time" : 1432945700, "msg" : "como va?" }, { "id" : "test", "time" : 1432945800, "msg" : "todo bien" } ], 200
		else:
			return { "error" : "token invalido" }, 401
	def post(self, user):
		root = request.get_json(force=True)
		if request.args.get('token', '') != token or request.args.get('r_user', '') !=  ide:
			return { "error": "token invalido" }, 401
		else:
			mensaje = root['message']
			print "llego mensaje" + mensaje
			return '', 201
			

# Agregamos los URIs para cada recurso
api.add_resource(login, '/login')
api.add_resource(usuarios, '/usuarios')
api.add_resource(usuario, '/usuario/<user>')
api.add_resource(conversacion, '/conversacion/<user>')

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
