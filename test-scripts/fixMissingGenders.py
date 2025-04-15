# We actually forgot to ask for people during registration for their sex on their document
# .......
# .....which is required by the italian law
# This script auto-sets both gender and sex on italian
# folks by getting their sex from the fiscal code and setting the gender according

import psycopg2

params = {
	"dbname":   "fzbackend",
	"user":     "fzbackend",
	"password": "",
	"host":     "localhost"
}

conn = None
try:
	print('Connecting to the PostgreSQL database...')
	conn = psycopg2.connect(**params)
	
	cur = conn.cursor()
	
	cur.execute('SELECT user_id, info_fiscal_code FROM membership_info WHERE info_fiscal_code IS NOT NULL;')
	rows = cur.fetchall()

	genders = [] # List "(userId, sex, gender)"

	for row in rows:
		code = row[1]
		try:
			birthDay = int(code[9:11]) #no pun intended
			if birthDay > 40:
				genders.append(f"({row[0]}, 'F', 'I prefer not to say')")
			else:
				genders.append(f"({row[0]}, 'M', 'I prefer not to say')")
		except Exception as e:
			print(f"Error while working on user {row[0]}: {repr(e)}")
	
	cur.close()
	cur = conn.cursor()

	values = ",".join(genders)
	#print(f'UPDATE membership_info SET info_document_sex = t.s, info_gender = t.g FROM (VALUES {values}) AS t(i, s, g) WHERE user_id = t.i;')
	res = cur.execute(f'UPDATE membership_info SET info_document_sex = t.s, info_gender = t.g FROM (VALUES {values}) AS t(i, s, g) WHERE user_id = t.i;')
	print(res)
	conn.commit()
	cur.close()
except (Exception, psycopg2.DatabaseError) as error:
	print(error)
finally:
	if conn is not None:
		conn.close()
		print('Database connection closed.')
