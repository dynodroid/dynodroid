from eventparser import parseEvents;
from actionparser import parseActions;
from xml.dom.minidom import Document;
import sys;

if len(sys.argv) != 3:
    sys.exit();

dir = sys.argv[1];
eventfile = dir + "SMSSend_monitoring.log";
filefile = dir + "KernelFile_monitoring.log";
networkfile = dir + "KernelNetwork_monitoring.log";
smsfile = dir + "SMSSend_monitoring.log";
urlfile = dir + "OutboundUrl_monitoring.log";

xml = Document()

pi = xml.createProcessingInstruction('xml-stylesheet',
     'type="text/xsl" href="' + sys.argv[2] + '/m3style/styler.xls"');
xml.appendChild(pi);

analysis = xml.createElement("analysis");
xml.appendChild(analysis);

testStrategy = xml.createElement("testStrategy");
testStrategy.setAttribute("name", "WidgetBasedRandomSelection");
analysis.appendChild(testStrategy);

events = xml.createElement("events");
testStrategy.appendChild(events);

actionset = xml.createElement("actionset");
testStrategy.appendChild(actionset);

parseEvents(eventfile, xml, events);

parseActions(filefile, xml, actionset, "file", events);
parseActions(networkfile, xml, actionset, "network", events);
parseActions(smsfile, xml, actionset, "sms", events);
parseActions(urlfile, xml, actionset, "url", events);

pretty = xml.toprettyxml(indent = " ");
file = open(sys.argv[1] + "output.xml", "w");
file.write(pretty);
file.close();
