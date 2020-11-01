import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const RPS = __ENV.RPS;
const VUS = parseInt(__ENV.VUS || 1);
const ITERATIONS = parseInt(__ENV.ITERATIONS || 1);
const MIN_DURATION_ITERATION = __ENV.MIN_DURATION_ITERATION;

let custom = {};

if (RPS) {
    custom.rps = parseInt(RPS);
}

export let options = Object.assign({
    vus: VUS,
    iterations: ITERATIONS,
    duration: '20m',
}, custom);

let sessionDuration = new Trend('session_duration');
let sessionWaiting = new Trend('session_waiting');
let sessionCount = new Counter('session_count');

const host = 'http://istio-ingressgateway.istio-system.svc.cluster.local';
const currencies = ['EUR', 'USD', 'JPY', 'CAD'];
const quantities = [1, 2, 3, 4, 5, 10];
const products = [
    '0PUK6V6EV0',
    '1YMWWN1N4O',
    '2ZYFJ3GM2N',
    '66VCHSJNUP',
    '6E92ZMYYFZ',
    '9SIQT8TOJO',
    'L9ECAV7KIM',
    'LS4PSXUNUM',
    'OLJCESPC7Z'
];

const success = response => response.status === 200;

const headers = {
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
};

function addSessionData(session, response) {
    if (success(response)) {
        session.duration += response.timings.duration;
        session.waiting += response.timings.waiting;
    }
}

export default function (data) {

    if (!MIN_DURATION_ITERATION) {
        iteration();
        return;
    }

    const before = new Date().getTime();

    iteration();

    const after = new Date().getTime();
    const diff = (after - before) / 1000;
    const remainder = (parseInt(MIN_DURATION_ITERATION) / 1000) - diff;

    if (remainder > 0) {
        sleep(remainder);
    } else {
        console.warn(
            `Timer exhausted! The execution time of the test took longer than ${MIN_DURATION_ITERATION} seconds. Remainder:${remainder}`
        );
    }
}

function iteration() {

    const session = {
        waiting: 0,
        duration: 0
    };

    request(index, 1, session);
    request(setCurrency, 1, session);
    request(browseProduct, 5, session);
    request(addToCart, 1, session);
    request(viewCart, 1, session);
    request(checkout, 1, session);

    endSession(session);

}

function random(array) {
    return array[Math.floor(Math.random() * array.length)];
}

function request(requestFunction, weight, session) {
    for (let i=0; i < weight; i++) {
        requestFunction(session);
    }
}

function endSession(session) {
    sessionDuration.add(session.duration);
    sessionWaiting.add(session.waiting);
    sessionCount.add(1);
}

function index(session) {

    let response = http.get(host + '/');

    addSessionData(session, response);

    check(response, {
        'index': success
    });
}

function setCurrency(session) {
    const currency = random(currencies);
    const data = {
        currency_code: currency
    };
    const response = http.post(host + '/setCurrency', data, headers);

    addSessionData(session, response);

    check(response, {
        'setCurrency': success
    });
}

function browseProduct(session) {
    const product = random(products);
    let response = http.get(host + '/product/' + product);

    addSessionData(session, response);

    check(response, {
        'browseProduct': success
    });
}

function addToCart(session) {
    // browseProduct
    const product = random(products);
    let responseBrowse = http.get(host + '/product/' + product);

    addSessionData(session, responseBrowse);

    check(responseBrowse, {
        'browseProduct': success
    });

    // addToCart
    const quantity = random(quantities);
    const data = {
        'product_id': product,
        'quantity': quantity
    };
    const responseAdd = http.post(host + '/cart', data, headers);

    addSessionData(session, responseAdd);

    check(responseAdd, {
        'addToCart': success
    });
}

function viewCart(session) {
    let response = http.get(host + '/cart');

    addSessionData(session, response);

    check(response, {
        'viewCart': success
    });
}

function checkout(session) {
    addToCart(session);
    const data = {
        'email': 'someone@example.com',
        'street_address': '1600 Amphitheatre Parkway',
        'zip_code': '94043',
        'city': 'Mountain View',
        'state': 'CA',
        'country': 'United States',
        'credit_card_number': '4432-8015-6152-0454',
        'credit_card_expiration_month': '1',
        'credit_card_expiration_year': '2039',
        'credit_card_cvv': '672'
    };

    const response = http.post(host + '/cart/checkout', data, headers);

    addSessionData(session, response);

    check(response, {
        'checkout': success
    });
}
