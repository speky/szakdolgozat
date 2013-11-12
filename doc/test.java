public List<DbData> queryAll() {
List<DbData> dataList = new ArrayList<DbData>();
Cursor cursor = db.rawQuery("SELECT * FROM "+DB_TABLE , null);
if (cursor != null) {
	cursor.moveToFirst();
while (cursor.isAfterLast() == false) {
	DbData data = cursorToData(cursor);
	dataList.add(data);
	cursor.moveToNext();
	}
cursor.close();
}
return dataList;
}
	