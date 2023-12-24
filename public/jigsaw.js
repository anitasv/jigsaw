

const $ = (id) => document.getElementById(id);

const getCenter = (elem) => {
    const rect = elem.getBoundingClientRect();
    return  [rect.x + rect.width / 2, rect.y + rect.height / 2];
}

const svgNs = "http://www.w3.org/2000/svg";

function svgCreate(name, attrs) {
    const node =
        document.createElementNS(svgNs, name);
    for (const key in attrs) {
        node.setAttributeNS(null, key, attrs[key]);
    }
    return node;
}


function redraw() {
    const M = parseInt($("m-input").value);
    const N = parseInt($("n-input").value);

    const outerBox = $("outerBox");

    const boxSize = 300;
    const boxGap = 50;
    const boxMultiplier = boxSize + boxGap;

    const outerWidth = (N * boxMultiplier + boxGap);
    const outerHeight = (M * boxMultiplier + boxGap);

    const scale = Math.min(500/outerWidth, 500/outerHeight);
    const transformWidth = -(1 - scale) * outerWidth / 2;
    const transformHeight = -(1 - scale) * outerHeight / 2

    outerBox.setAttributeNS(null, "width",
        "" + outerWidth);
    outerBox.setAttributeNS(null, "height",
        "" + outerHeight);

    outerBox.setAttributeNS(null, "transform",
        "translate(" + transformWidth + "," + transformHeight + ")"
     + " scale(" + scale + ")");

    const newBoxes = [];

    const H = []; // bottom of each cell.
    const V = []; // right of each cell.

    for (let m = 0; m < M; m++) {
        for (let n = 0; n < N; n++) {
            V.push(Math.random() < 0.5 ? '<' : '>');
            H.push(Math.random() < 0.5 ? '<' : '>');
        }
    }

    // bottom most row correction
    for (let n = 0; n < N; n++) {
        H[(M-1) * N + n] = '|';
    }
    // right most column correction.
    for (let m = 0; m < M; m++) {
        V[m * N + (N-1)] = '|';
    }

    // const rect = svgCreate("rect", {
    //     "x": ("" + (boxGap + (n * boxMultiplier))),
    //     "y": ("" + (boxGap + (m * boxMultiplier))),
    //     "height": "" + boxSize,
    //     "width": "" + boxSize,
    //     "style": "fill: rgb(255,255,255);stroke-width:1;stroke:rgb(0,0,0)"
    // });

    const getTopPath = (poke) => {
        if (poke === '|') {
            return 'h 300';
        } else if (poke === '<') {
            return "c 20 -40, 85 -40, 100 0\n" +
                "s 85 40, 100 0\n" +
                "s 85 -15, 100 0";
        } else {
            return "c 20 40, 85 40, 100 0\n" +
                "s 85 -40, 100 0\n" +
                "s 85 15, 100 0";
        }
    }

    const getRightPath = (poke) => {
        if (poke === '|') {
            return 'v 300';
        } else if (poke === '<') {
            return "c 40 20, 40 85, 0 100\n" +
                "s -40 85, 0 100\n" +
                "s 15 85, 0 100";
        } else {
            return "c -40 20, -40 85, 0 100\n" +
                "s 40 85, 0 100\n" +
                "s -15 85, 0 100";
        }
    }
    const getBottomPath = (poke) => {
        if (poke === '|') {
            return 'h -300';
        } else if (poke === '<') {
            return "c -20 40, -85 40, -100 0\n" +
                "s -85 -40, -100 0\n" +
                "s -85 15 -100 0";
        } else {
            return "c -20 -40, -85 -40, -100 0\n" +
                "s -85 40, -100 0\n" +
                "s -85 -15 -100 0";
        }
    }
    const getLeftPath = (poke) => {
        if (poke === '|') {
            return 'v -300';
        } else if (poke === '<') {
            return "c -40 -20, -40 -85, 0 -100\n" +
                "s 40 -85, 0 -100\n" +
                "s -15 -85, 0 -100";
        } else {
            return "c 40 -20, 40 -85, 0 -100\n" +
                "s -40 -85, 0 -100\n" +
                "s 15 -85, 0 -100";
        }
    }

    const flip = (poke) => {
        if (poke === "<") {
            return ">"
        } else if (poke === ">") {
            return "<";
        } else {
            return poke;
        }
    }

    for (let m = 0; m < M; m++) {
        for (let n = 0; n < N; n++) {
            let d = "M " + (boxGap + (n * boxMultiplier)) +
                " " + (boxGap + (m * boxMultiplier));

            const topPath =
                getTopPath((m > 0) ? flip(H[(m-1) * N + n]) : '|') ;
            const rightPath =
                getRightPath(V[m * N + n]);
            const botPath =
                getBottomPath(H[m * N + n]);
            const leftPath =
                getLeftPath((n > 0) ?flip(V[m * N + (n - 1)]) : '|') ;

            d = d + topPath + rightPath + botPath + leftPath;
            const path = svgCreate("path", {
                "d": d,
                "fill": "cornflowerblue",
                "stroke": "black",
                "stroke-width": "10"
            })
            newBoxes.push(path);
        }
    }
    outerBox.replaceChildren(...newBoxes);
}

function main() {


    redraw();
    $("redraw").addEventListener('click', evt => {
        redraw();
    });

}

main();
