from openstack import connection
from openstack import utils
import logging
import sys

# Enable requests logging
logger = logging.getLogger('requests')
formatter = logging.Formatter(
    '%(asctime)s %(levelname)s: %(name)s %(message)s')
console = logging.StreamHandler(sys.stdout)
console.setFormatter(formatter)
logger.setLevel(logging.DEBUG)
logger.addHandler(console)

# Enable logging to console
utils.enable_logging(debug=True, stream=sys.stdout)


# Get connection
conn = connection.Connection(auth_url="http://localhost:5000/v2.0",
                             project_name="admin",
                             username="admin",
                             password="61f23b78184d4b92")

# Get network API
network = conn.network.find_network("watshnet")
if network is None:
    network = conn.network.create_network(name="watshnet")
else:
    print(network)