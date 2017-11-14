# dynodroid
Automatic Input Generation System for Android Apps

# Setup
1. Clone the repository:
```
git clone git@github.com:dynodroid/dynodroid.git <local_repo_path>
```
2. Download the sdcard image from the google drive link: https://drive.google.com/open?id=1mf4kKCNgz059C5hzRKOT5Wk_e9WccmFk
3. Copy the downloaded sdcard image into setup location:
```
cp <path_to_the_downloaded_file> <local_repo_path>/dynodroidsetup/freshavd/emu.avd/sdcard.img
```
4. Follow the instructions in the [user doc](https://github.com/dynodroid/dynodroid/blob/master/docs/dd_use.pdf)

# Self-contained VHD
* You can download a self-contained VHD from the [Google Drive](https://drive.google.com/open?id=0B4XwT5D6qkNmVTd6VlVyaE9DbUE). 
* Create a VirtualBox VM (select ubuntu, 32-bit) and load the above-downloaded VHD.
* The username and password for the VHD are both `dynodroid`.

## Using the VHD
The VHD will be running ubuntu 12.02, 32-bit OS. Login into the VHD under user-name and password as `dynodroid`.
To test an app, follow the below instructions inside the VHD:
1. Download the apk you want to test and copy into apps folder:
```
cp <path_to_apk_inside_VHD> /home/dynodroid/Desktop/dynodroiddeployment/apps/
```
2. [Optional] Edit the file: `/home/dynodroid/Desktop/dynodroiddeployment/dynodroid.properties` according to the [user doc](https://github.com/dynodroid/dynodroid/blob/master/docs/dd_use.pdf).
3. Run
```
cd /home/dynodroid/Desktop/dynodroiddeployment
ant run
```
