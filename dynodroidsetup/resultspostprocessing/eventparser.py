import re;

def parseEvents(location, xml, parent):

    file = open(location,"r");

    for line in file:
        if re.match("[0-9]+", line) == None:
            continue;
        ets = re.search("[0-9]+", line);

        event = xml.createElement("event");
        event.setAttribute("id", ets.group(0));
        parent.appendChild(event);
        
        if "," in line:
            edisc = re.search("(^[0-9]+):\(?(.*?)\)", line);
        else:
            edisc = re.search("(^[0-9]+):?(.*)", line);
        if edisc != None and len(edisc.groups()) > 1 and edisc.group(2) != None:
            if re.match("-?[0-9]+([-_0-9]+)+:", edisc.group(2)):
                eid = re.match("-?[0-9]+([-_0-9]+)+", edisc.group(2)).group(0);
                event.setAttribute("sid", eid);
                event.setAttribute("wid", re.match("^.*_(.*)", eid).group(1));
            if "," in edisc.group(2):
                elements = re.split("(^([-_0-9]+):?)?(.*?),", edisc.group(2));
            else:
                elements = re.split("(.*?):", edisc.group(2));
        else:
            continue;

        name = xml.createElement("name");
        if elements[0]=="":
            if len(elements)>4:
                name.appendChild(xml.createTextNode(elements[3]));
            else:
                name.appendChild(xml.createTextNode(elements[1]));
        else:
            name.appendChild(xml.createTextNode(elements[0]));
        event.appendChild(name);

        if len(elements) >= 1 and elements[-1] != "":
            dtext = xml.createTextNode(elements[-1]);
        else:
            continue;

        description = xml.createElement("description");
        description.appendChild(dtext);
        event.appendChild(description);

    file.close();
