import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const success = response => response.status === 200;

let sessionDuration = new Trend('session_duration');
let sessionWaiting = new Trend('session_waiting');
let sessionCount = new Counter('session_count');

export let options = {
    vus: 100,
    iterations: 100,
    duration: '20m',
};

const login = 'uid0@email.com';
const password = 'password';

const host = 'http://istio-ingressgateway.istio-system.svc.cluster.local';
// const host = 'http://192.168.1.202';
// const host = 'http://192.168.1.202:31952';

const authService = host + '/auth';
const customerService = host + '/customer';
const acmeairService = host + '/acmeair';
const flightService = host + '/flight';
const bookingService = host + '/booking';

function debug(response) {
    console.log(JSON.stringify(response, null, 2));
}

function indexTest(session) {
    const response = http.get(acmeairService + '/index.html');
    addSessionData(session, response);
    check(response, {
        'index': success
    });
}

function loginTest(session) {

    const formData = {
        login,
        password
    };

    const response = http.post(authService + '/login', formData);
    addSessionData(session, response);
    check(response, {
        'login': success
    });

    // return response.cookies['Bearer'][0]['value'];
}

function updateAccountTest(session) {
    const formData = JSON.stringify({
        '_id': login,
        'password': password,
        'phoneNumber': '919-123-4567',
        'phoneNumberType': 'BUSINESS',
        'status': 'GOLD',
        'total_miles': 0,
        'miles_ytd': 0,
        'address': {
            'streetAddress1': '123 Main St.',
            'city': 'Anytown',
            'stateProvince': 'NC',
            'country': 'USA',
            'postalCode': '27617'
        }
    });

    const params = {
        headers: {
            'Content-Type': 'application/json'
        }
    }

    const response = http.post(customerService + '/byid/' + login, formData, params);
    addSessionData(session, response);
    check(response, {
        'updateAccount': success
    });
}

function flightsTest(session) {
    const response = http.get(acmeairService + '/query.html');
    addSessionData(session, response);
    check(response, {
        'flights': success
    });
}

function findFlightsTest(session) {

    const fromDate = new Date(new Date().setHours(0,0,0,0)).toString();
    const returnDate = new Date(new Date().setHours(23,59,59,999)).toString();
    
    const formData = {
        'fromAirport': 'AMS',
        'toAirport': 'BOM',
        'fromDate': fromDate,
        'returnDate': returnDate,
        'oneWay': 'true'
    };

    const response = http.post(flightService + '/queryflights', formData);
    addSessionData(session, response);
    check(response, {
        'findFlights': success
    });
}

function bookFlightsTest(session) {
    const formData = {
        'userid': login,
        'toFlightId': '80834210-6050-4758-8488-1f4b772f77df',
        'toFlightSegId': 'AA0',
        'oneWayFlight': 'true'
    };

    const response = http.post(bookingService + '/bookflights', formData);
    addSessionData(session, response);
    check(response, {
        'bookFlights': success
    });
}

function checkinTest(session) {
    const response = http.get(acmeairService + '/checkin.html');
    addSessionData(session, response);
    check(response, {
        'checkin': success
    });

    // search grid
    findBookingByUserTest(session);
}

function findBookingByUserTest(session) {
    const response = http.get(bookingService + '/byuser/' + login);
    addSessionData(session, response);
    check(response, {
        'findBookingByUser': success
    });
}

function cancelBookingTest(session) {
    const formData = {
        'userid': login,
        'number': 'bd083c3c-f6cf-4edf-b4e5-dde40a902aaa'
    };

    const response = http.post(bookingService + '/cancelbooking', formData);
    addSessionData(session, response);
    check(response, {
        'cancelBooking': success
    });
}

function iteration() {
    const session = {
        waiting: 0,
        duration: 0
    };

    request(indexTest, 1, session);
    request(loginTest, 1, session);
    request(updateAccountTest, 1, session);
    request(flightsTest, 1, session);
    request(findFlightsTest, 5, session);
    request(bookFlightsTest, 1, session);
    request(checkinTest, 1, session);
    request(cancelBookingTest, 1, session);

    endSession(session);
}

function request(requestFunction, weight, session) {
    for (let i=0; i < weight; i++) {
        requestFunction(session);
    }
}

function addSessionData(session, response) {
    if (success(response)) {
        session.duration += response.timings.duration;
        session.waiting += response.timings.waiting;
    }
}

function endSession(session) {
    sessionDuration.add(session.duration);
    sessionWaiting.add(session.waiting);
    sessionCount.add(1);
}

export default function () {
    iteration();
}