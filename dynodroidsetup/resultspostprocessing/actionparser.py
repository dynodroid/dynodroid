import re;

def parseActions(location, xml, parent, mode, events):
    actions = xml.createElement("actions");
    actions.setAttribute("type", mode);
    parent.appendChild(actions);

    eventex = "[0-9]+";

    if mode == "network":
        regex = "(addr=|port=)(.*?)(,|$)";
        negex = "[0-9a-f]{4}:";
        count = 2;
        tagnames = ["address", "port"];
    elif mode == "sms":
        regex = "(dest=|text=)(.*?)(;|$)";
        negex = None;
        count = 2;
        tagnames = ["destination", "text"];
    elif mode == "file":
        regex = "(filepath=)(.*)";
        negex = "/sys|/dev|classes.dex|/dev/ashmem|/Come over! lets have a coffe";
        count = 1;
        tagnames = ["filepath"];
    elif mode == "url":
        regex = "(Host=)(.*?)(;|$)";
        negex = None;
        count = 1;
        tagnames = ["server"];

    file = open(location, "r");

    event = None;
    for line in file:
        eid = re.match(eventex, line);
        if eid != None:
            event = eid.group(0);

        if event == None or (negex != None and re.search(negex, line)):
            continue;

        result = re.findall(regex, line);

        if count <= len(result):
            action = xml.createElement("action");
            action.setAttribute("event", "#"+event);

            for itr in range(count):
                tag = xml.createElement(tagnames[itr]);
                tag.appendChild(xml.createTextNode(result[itr][1]));
                action.appendChild(tag);

            actions.appendChild(action);

#            action = action.cloneNode(True);
#            action.setAttribute("type", mode);
#            action.removeAttribute("event");
#            evnodes = events.getElementsByTagName("event");
#            for evnode in evnodes:
#                if evnode.getAttribute("id") == event:
#                   evnode.appendChild(action);

    file.close();
