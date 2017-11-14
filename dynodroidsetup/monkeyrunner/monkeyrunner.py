import re;
import sys;
import socket;
from time import sleep;
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

if len(sys.argv) != 2:
    sys.exit(0);

lookup = [0]*100;
lookup[4] = 'KEYCODE_BACK';
lookup[62] = 'KEYCODE_SPACE';
lookup[66] = 'KEYCODE_ENTER';
lookup[82] = 'KEYCODE_MENU';

DOWN = MonkeyDevice.DOWN;
UP = MonkeyDevice.UP;
DOWN_AND_UP = MonkeyDevice.DOWN_AND_UP;

rex = [
    "(^Tap\()(.*?)\)",
    "(^DispatchKey\()(.*?)\)",
    "(^UserWait\()(.*?)\)",
    "(^Input:)(.*)",
    "(^Slide\()(.*?)\)",
    "(^LongTap\()(.*?)\)",
    "(^Snap\(\):)(.*)",
    "(^Snap\()(.*?)\):(.*)"]

line = "";
port = 9000 + int(sys.argv[1][len(sys.argv[1])-3:]);
try:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM);
    sock.connect(('127.0.0.1', port));
except socket.error, e:
    print "Socket connect error:" + e.message;
    sys.exit();

print "Socket connected " + sock.getpeername()[0] + ":" + unicode(port);
sock.sendall("Socket connected\n");

device = MonkeyRunner.waitForConnection(10.0,sys.argv[1]);
if device == None:
    print "Failed to get device";
    sys.exit();

while line != "End Data":
    try:
        line = sock.recv(10240);
        print line;
    except socket.error, e:
        print "Read error:" + e.message;
        break;

    try:
        sock.sendall("Trying to execute: " + line);
    except socket.error, e:
        print "Send error:" + e.message;
        break;
        
    print "Trying to execute: " + line;

    for i in range(len(rex)):
        match = re.match(rex[i], line);
        if match != None:
            break;

    if match != None:
        if i == 0:
            coord = match.group(2).split(',');
            x = int(coord[0], 10);
            y = int(coord[1], 10);
            device.touch(x,y,DOWN_AND_UP);
        elif i == 1:
            k = int(match.group(2).split(',')[2], 10);
            device.press(lookup[k], DOWN);
            device.press(lookup[k], UP);
        elif i == 2:
            sleep(sleeptime);
        elif i == 3:
            sleep(1);
            for line in match.group(2).split('\\n'):
                wc = 0;
                for word in line.split(' '):
                    if wc > 1:
                        device.press(lookup[62], DOWN);
                        device.press(lookup[62], UP);
                    device.type(word);
                    wc += 1;
                device.press(lookup[66], DOWN);
                device.press(lookup[66], UP);
        elif i == 4:
            coord = match.group(2).split(',');
            if len(coord) == 4:
                x1 = int(coord[0], 10);
                y1 = int(coord[1], 10);
                x2 = int(coord[2], 10);
                y2 = int(coord[3], 10);
                device.drag((x1,y1), (x2,y2));
        elif i == 5:
            coord = match.group(2).split(',');
            x = int(coord[0], 10);
            y = int(coord[1], 10);
            device.touch(x,y,DOWN);
            sleep(1);
            device.touch(x,y,UP);
        elif i == 6:
            try:
                device.takeSnapshot().writeToFile(match.group(2));
            except Exception, e:
                try:
                    sock.sendall("ERROR: " + e.message);
                except socket.error, e:
                    print "Send error:" + e.message;
        elif i == 7:
            tup = match.group(2).split(',');
            if len(tup) == 4:
                tup = (int(tup[0]), int(tup[1]), int(tup[2]) - int(tup[0]), int(tup[3]) - int(tup[1]));
                try:
                    device.takeSnapshot().getSubImage(tup).writeToFile(match.group(3));
                except Exception, e:
                    try:
                        sock.sendall("ERROR: " + e.message + "\n");
                    except socket.error, e:
                        print "Send error:" + e.message;
        try:
            print "Done" + line;
            sock.sendall("Completed execution successfully: " + line + "\n");
        except socket.error, e:
            print "Send error:" + e.message;
    else:
        try:
            sock.sendall("Command not recognized: " + line + "\n");
        except socket.error, e:
            print "Send error:" + e.message;

print "Monkeyrunner exited successfully";
