import { Vec2d } from "./primitives/Vec2d";
/**
 * @desc 贝塞尔曲线算法，包含了3阶贝塞尔
 */
export declare class Bezier {
    static bezier(num: number, points: Vec2d[]): Vec2d[];
    /**
     * @desc 获取点，这里可以设置点的个数
     * @param {number} num 点个数
     * @param {Vec2d} p1 点坐标
     * @param {Vec2d} p2 点坐标
     * @param {Vec2d} p3 点坐标
     * @param {Vec2d} p4 点坐标
     * 如果参数是 num, p1, p2 为一阶贝塞尔
     * 如果参数是 num, p1, c1, p2 为二阶贝塞尔
     * 如果参数是 num, p1, c1, c2, p2 为三阶贝塞尔
     */
    static getBezierPoints(num: number | undefined, p1: Vec2d, p2: Vec2d, p3?: Vec2d, p4?: Vec2d): Vec2d[];
    /**
     * @desc 一阶贝塞尔
     * @param {number} t 当前百分比
     * @param {Vec2d} p1 起点坐标
     * @param {Vec2d} p2 终点坐标
     */
    static oneBezier(t: number, p1: Vec2d, p2: Vec2d): Vec2d;
    /**
     * @desc 二阶贝塞尔
     * @param {number} t 当前百分比
     * @param {Array} p1 起点坐标
     * @param {Array} p2 终点坐标
     * @param {Array} cp 控制点
     */
    static twoBezier(t: number, p1: Vec2d, cp: Vec2d, p2: Vec2d): Vec2d;
    /**
     * @desc 三阶贝塞尔
     * @param {number} t 当前百分比
     * @param {Array} p1 起点坐标
     * @param {Array} p2 终点坐标
     * @param {Array} cp1 控制点1
     * @param {Array} cp2 控制点2
     */
    static threeBezier(t: number, p1: Vec2d, cp1: Vec2d, cp2: Vec2d, p2: Vec2d): Vec2d;
}
