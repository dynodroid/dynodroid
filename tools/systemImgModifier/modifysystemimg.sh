mkdir extractedSys
cd extractedSys
../unyaffs $1
cp ../emma.jar framework/emma.jar
cd ..
./mkyaffs2image extractedSys newsystem.img
rm -rf extractedSys


