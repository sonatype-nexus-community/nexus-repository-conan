class PingHandler:
    def __init__(self):
        pass

    def handle(self, context):
        print("Ping from python")
        return ResponseBuilder().status(Status(True, 200)).build()
        # return HttpResponses.ok()