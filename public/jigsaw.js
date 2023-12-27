

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

const protoLine = "c 20 30 20 70 0 100\n" +
    "c -10 10 -10 10 -30 10\n" +
    "c -10 0 -10 -10 -30 -10\n" +
    "c -10 0 -20 0 -30 10\n" +
    "c -10 20 -10 30 -10 40\n" +
    "c 0 10 0 20 10 40\n" +
    "c 10 10 20 10 20 10\n" +
    "c 20 0 30 -10 40 -10\n" +
    "c 10 0 20 0 30 10\n" +
    "c 20 30 20 70 0 100";

const curved = (mx, my, rotate, scale) => {
    let out = '';
    const splits = protoLine.split("\n");
    for (const curve of splits) {
        const parts = curve.split(" ");
        out += parts[0] + " ";
        for (let i = 1; i < parts.length; i += 2) {
            let x = parseFloat(parts[i]) * mx;
            let y = parseFloat(parts[i + 1]) * my;
            if (rotate) {
                let t = x;
                x = y;
                y = t;
            }
            out += (x * scale) + " " + (y * scale) + " ";
        }
        out += '\n';
    }
    return out;
}

function drawJigs(outerBox, withGap, M, N, J, isSoln) {

    const boxSize = 300;
    const boxGap = withGap ? 220 : 5;
    const boxMultiplier = boxGap + boxSize;

    const outerWidth = (N * (boxSize + boxGap));
    const outerHeight = (M * (boxSize + boxGap));

    const scale = Math.min(500/outerWidth, 500/outerHeight);
    const transformWidth = scale * outerWidth;
    const transformHeight = scale * outerHeight;

    outerBox.setAttributeNS(null, "width",
        "" + transformWidth);
    outerBox.setAttributeNS(null, "height",
        "" + transformHeight);
    //
    // outerBox.setAttributeNS(null, "transform",
    //     "scale(" + scale + ")");

    const newPaths = [];
    const newRects = [];

    const getTopPath = (poke) => {
        if (poke === '-') {
            return 'h ' + (300 * scale);
        } else if (poke === '<') {
            return curved(-1, 1, true, scale);
        } else {
            return curved(+1, 1, true, scale);
        }
    }

    const getRightPath = (poke) => {
        if (poke === '-') {
            return 'v ' + (300 * scale);
        } else if (poke === '<') {
            return curved(1, 1, false, scale);
        } else {
            return curved(-1, 1, false, scale);
        }
    }
    const getBottomPath = (poke) => {
        if (poke === '-') {
            return 'h ' + (-300 * scale);
        } else if (poke === '<') {
            return curved(1, -1, true, scale);
        } else {
            return curved(-1, -1, true, scale);
        }
    }
    const getLeftPath = (poke) => {
        if (poke === '-') {
            return 'v ' + (-300 * scale);
        } else if (poke === '<') {
            return curved(-1, -1, false, scale);
        } else {
            return curved(1, -1, false, scale);
        }
    }

    const pathGen = (piece, m, n) => {
        const x = (boxGap/2 + (n * boxMultiplier)) * scale;
        const y = (boxGap/2 + (m * boxMultiplier)) * scale;
        const topPath = getTopPath(piece[0])
        const rightPath = getRightPath(piece[1])
        const botPath = getBottomPath(piece[2]);
        const leftPath =getLeftPath(piece[3]);

        return "M " + x + " " + y + topPath + rightPath + botPath + leftPath;
    }

    for (let m = 0; m < M; m++) {
        for (let n = 0; n < N; n++) {
            const currentIndex = m * N + n;

            if (J[currentIndex]) {
                const path = svgCreate("path", {
                    "d": pathGen(J[currentIndex], m, n),
                    "fill": "cornflowerblue",
                    "stroke": "black",
                    "stroke-width": 10 * scale,
                });

                path.addEventListener('click', clickHandler(m, n, path, pathGen))
                if (!isSoln) {
                    path.addEventListener('mouseover', (evt) => {
                        path.setAttributeNS(null, 'fill', 'yellow');
                        if (solved) {
                            const soln = solution[m * N + n];
                            const mark = bridge.output.newPaths[soln[0] * N + soln[1]];
                            mark.setAttributeNS(null, 'fill', 'white');
                        }
                    });
                    path.addEventListener('mouseout', (evt) => {
                        path.setAttributeNS(null, 'fill', 'cornflowerblue');
                        if (solved) {
                            const soln = solution[m * N + n];
                            const mark = bridge.output.newPaths[soln[0] * N + soln[1]];
                            mark.setAttributeNS(null, 'fill', 'cornflowerblue');
                        }
                    });
                } else {
                    // path.addEventListener('mouseover', (evt) => {
                    //     path.setAttributeNS(null, 'fill', 'yellow');
                    //     if (solved) {
                    //         const soln = solution[m * N + n];
                    //         const mark = bridge.output.newRects[soln[0] * N + soln[1]];
                    //         mark.setAttributeNS(null, 'fill', 'red');
                    //     }
                    // });
                    // path.addEventListener('mouseout', (evt) => {
                    //     path.setAttributeNS(null, 'fill', 'cornflowerblue');
                    //     if (solved) {
                    //         const soln = solution[m * N + n];
                    //         const mark = bridge.output.newRects[soln[0] * N + soln[1]];
                    //         mark.setAttributeNS(null, 'fill', 'transparent');
                    //     }
                    // });
                }

                // path.addEventListener('click', (evt) => {
                //     path.setAttributeNS(null, 'fill', 'cornflowerblue');
                //     if (solved) {
                //         const soln = solution[m * N + n];
                //         const solnIndex = soln[0] * N + soln[1];
                //         const newPiece = [];
                //         for (let s = 0; s < 4; s++) {
                //             newPiece.push(J[currentIndex][(s + soln[2]) % 4]);
                //         }
                //         J_right[solnIndex] = newPiece;
                //         drawJigs($("rightBox"), true, M, N, J_right);
                //     }
                // });
                newPaths.push(path);
            }

            if (withGap) {
                const x = (n * boxMultiplier);
                const y = (m * boxMultiplier);

                const rect = svgCreate("rect", {
                    "x": ("" + x * scale),
                    "y": ("" + y * scale),
                    "height": "" + boxMultiplier * scale,
                    "width": "" + boxMultiplier * scale,
                    "stroke-width": 2 * scale,
                    "stroke" : "black",
                    "fill": "transparent"
                });
                newRects.push(rect);
            }
        }
    }

    if (!isSoln) {
        bridge.input.newPaths = newPaths;
        bridge.input.newRects = newRects;
    } else {
        bridge.output.newPaths = newPaths;
        bridge.output.newRects = newRects;
    }

    const newBoxes = newRects.concat(newPaths);
    outerBox.replaceChildren(...newBoxes);
}

function drawOuter() {
    drawJigs($("outerBox"), true, M, N, J, false);
}

function drawRight() {
    drawJigs($("rightBox"), expanded, M, N, J_right, true);
}

let J = [];
let M = 1;
let N = 1;
let expanded = true;
let solution = [];
let solved = true;
let J_right = [];
const bridge = {
    input: {},
    output: {}
}

function rotatePiece(piece, s) {
    const rotatedPiece = [];
    for (let k = 0; k < 4; k++) {
        rotatedPiece[k] = piece[(k + s) % 4];
    }
    return rotatedPiece;
}

function clickHandler(m, n, path, pathGen) {
    return evt => {
        resetSolution();
        J[m * N + n] = rotatePiece(J[m * N + n],3);
        path.setAttributeNS(null ,  "d",
            pathGen(J[m * N + n], m, n));
    }
}


function redraw() {
    resetSolution();

    M = parseInt($("m-input").value);
    N = parseInt($("n-input").value);

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
        H[(M-1) * N + n] = '-';
    }
    // right most column correction.
    for (let m = 0; m < M; m++) {
        V[m * N + (N-1)] = '-';
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
    J = [];

    for (let m = 0; m < M; m++) {
        for (let n = 0; n < N; n++) {

            const sides = [];

            sides.push((m > 0) ? flip(H[(m-1) * N + n]) : '-');
            sides.push(V[m * N + n]);
            sides.push(H[m * N + n]);
            sides.push((n > 0) ? flip(V[m * N + (n - 1)]) : '-') ;

            J.push(sides);
        }
    }
    drawOuter();
}

function resetSolution() {
    solved = false;
    solution = [];
    J_right = [];
    $("rightBox").replaceChildren();
}
function randInt(bound) {
    const p = Math.floor(Math.random() * bound);
    if (p === bound) {
        return bound - 1;
    } else {
        return p;
    }
}

function shuffle() {
    resetSolution();
    for (let i = 0; i < J.length; i++) {
        let j = i + randInt(J.length - i);
        const nextPiece = J[j];
        J[j] = J[i];

        let s = randInt(4);
        J[i] = rotatePiece(nextPiece, s);
    }
    drawOuter();
}

function expand() {
    expanded = !expanded;
    if (expanded) {
        $("expand").value = 'Collapse';
    } else {
        $("expand").value = 'Spread';
    }
    if (solved) {
        drawRight();
    }
}

function solve() {
    resetSolution();
    const repr = piece => piece.join(",");
    let problem = '';
    for (let i = 0; i < J.length; i++) {
        problem += repr(J[i]);
        if (i !== J.length - 1) {
            problem += "\n";
        }
    }
    const result = fetch("/jigsaw/solve?M=" + M + "&N=" + N, {
        "method": "POST",
        "body": problem
    });
    result.then(val => {
        if (val.ok) {
            val.text().then(text => {
                const splits = text.split("\n");
                const line1 = splits[0];
                if (line1.startsWith("UNSOLVABLE:")) {
                    console.log(line1);
                }
                solution = splits.map(line =>
                    line.split(",").map(x => parseInt(x))
                );
                solved = true;
                drawOuter();

                J_right = []
                for (let i = 0; i < J.length; i++) {
                    J_right[i] = null;
                }
                for (let i = 0; i < J.length; i++) {
                    const soln = solution[i];
                    const solnIndex = soln[0] * N + soln[1];
                    const newPiece = [];
                    for (let s = 0; s < 4; s++) {
                        newPiece.push(J[i][(s + soln[2]) % 4]);
                    }
                    J_right[solnIndex] = newPiece;
                }
                drawRight();
            });
        } else {
            console.log(val.status);
        }
    });
}

function main() {
    redraw();
    $("redraw").addEventListener('click', evt => {
        redraw();
    });

    $("expand").addEventListener('click', (evt => {
        expand();
    }))

    $("shuffle").addEventListener('click', (evt => {
        shuffle();
    }))
    $("solve").addEventListener('click', (evt) => {
        solve();
    });
}

main();
